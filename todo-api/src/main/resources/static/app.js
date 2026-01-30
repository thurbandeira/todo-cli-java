const state = {
  baseUrl: localStorage.getItem("baseUrl") || "http://localhost:8080",
  token: localStorage.getItem("token") || "",
  username: localStorage.getItem("username") || "",
  page: 0,
  size: 8,
  last: true,
  currentStatus: "all",
  chart: null,
  tasksCache: [],
  activity: [],
};

const el = (id) => document.getElementById(id);
const toastEl = el("toast");
let toastTimeout = null;

function toast(message, type = "success") {
  toastEl.textContent = message;
  toastEl.className = `toast show ${type}`;
  if (toastTimeout) clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    toastEl.className = "toast";
  }, 2500);
  recordActivity(message, type);
}

function setAuthStatus() {
  const status = el("authStatus");
  if (state.token) {
    status.textContent = "Autenticado";
    status.style.color = "#6ee7b7";
  } else {
    status.textContent = "Nao autenticado";
    status.style.color = "#b8c0d9";
  }
  const openAuthBtn = el("openAuthBtn");
  const logoutBtn = el("logoutBtn");
  if (state.token) {
    openAuthBtn.textContent = "Conta";
    logoutBtn.disabled = false;
  } else {
    openAuthBtn.textContent = "Entrar";
    logoutBtn.disabled = true;
  }
}

function setActiveUser(name) {
  el("activeUser").textContent = name ? `Usuario: ${name}` : "Usuario: -";
  el("profileName").textContent = name || "Visitante";
  el("profileRole").textContent = name ? "Membro" : "Sem sessao";
}

function saveToken(token, username = "") {
  state.token = token || "";
  localStorage.setItem("token", state.token);
  if (username) {
    state.username = username;
    localStorage.setItem("username", username);
  }
  setActiveUser(state.username);
  setAuthStatus();
}

function saveBaseUrl(url) {
  state.baseUrl = url;
  localStorage.setItem("baseUrl", state.baseUrl);
}

async function request(path, options = {}) {
  if (!state.token && path.startsWith("/api/tasks")) {
    throw new Error("Faca login para acessar as tarefas.");
  }
  const url = `${state.baseUrl}${path}`;
  const headers = options.headers || {};
  headers.Accept = "application/json";
  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }
  const response = await fetch(url, { ...options, headers });
  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }
  if (response.status === 401 && state.token) {
    const refreshed = await refreshToken();
    if (refreshed) {
      return request(path, options);
    }
  }
  if (!response.ok) {
    const msg = data && data.detail ? data.detail : response.statusText;
    throw new Error(msg);
  }
  return data;
}

function recordActivity(message, type) {
  const list = el("activityFeed");
  if (!list) return;
  state.activity.unshift({ message, type, time: new Date() });
  state.activity = state.activity.slice(0, 6);
  list.innerHTML = "";
  state.activity.forEach((entry) => {
    const item = document.createElement("li");
    item.className = "activity-item";
    const dot = document.createElement("span");
    dot.className = `activity-dot ${entry.type}`;
    const text = document.createElement("span");
    text.textContent = entry.message;
    item.appendChild(dot);
    item.appendChild(text);
    list.appendChild(item);
  });
}

async function refreshToken() {
  try {
    const response = await fetch(`${state.baseUrl}/api/auth/refresh`, {
      method: "POST",
      headers: { Authorization: `Bearer ${state.token}` },
    });
    if (!response.ok) return false;
    const data = await response.json();
    saveToken(data.token, state.username);
    toast("Token renovado.");
    return true;
  } catch {
    return false;
  }
}

async function register() {
  const username = el("username").value.trim();
  const password = el("password").value.trim();
  if (!username || !password) {
    toast("Informe usuario e senha.", "error");
    return;
  }
  const data = await request("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  saveToken(data.token, username);
  toast("Conta criada e autenticado.");
  await listAll();
}

async function login() {
  const username = el("username").value.trim();
  const password = el("password").value.trim();
  if (!username || !password) {
    toast("Informe usuario e senha.", "error");
    return;
  }
  const data = await request("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  saveToken(data.token, username);
  toast("Login realizado.");
  await listAll();
}

function logout() {
  saveToken("", "");
  renderTasks([], "taskList");
  renderTasks([], "taskListTasks");
  renderKanban([]);
  toast("Logout.");
}

function openModal(task) {
  const modal = el("modal");
  el("modalTitle").value = task.title;
  modal.dataset.taskId = task.id;
  modal.classList.add("show");
}

function closeModal() {
  el("modal").classList.remove("show");
}

async function saveModal() {
  const id = el("modal").dataset.taskId;
  const title = el("modalTitle").value.trim();
  if (!title) {
    toast("Titulo invalido.", "error");
    return;
  }
  await request(`/api/tasks/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title }),
  });
  closeModal();
  toast(`Tarefa #${id} atualizada.`);
  await listPaged(state.page, state.currentStatus);
}

async function addTask(inputId) {
  const title = el(inputId).value.trim();
  if (!title) {
    toast("Titulo invalido.", "error");
    return;
  }
  const data = await request("/api/tasks", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title }),
  });
  el(inputId).value = "";
  toast(`Tarefa criada (#${data.id}).`);
  await listPaged(0, state.currentStatus);
}

async function completeTask(taskId) {
  await request(`/api/tasks/${taskId}/complete`, { method: "POST" });
  toast(`Tarefa #${taskId} concluida.`);
  await listPaged(state.page, state.currentStatus);
}

async function removeTask(taskId) {
  await request(`/api/tasks/${taskId}`, { method: "DELETE" });
  toast(`Tarefa #${taskId} removida.`);
  await listPaged(state.page, state.currentStatus);
}

function renderTasks(tasks, listId) {
  const list = el(listId);
  list.innerHTML = "";
  if (!tasks.length) {
    const empty = document.createElement("li");
    empty.className = "list-empty";
    empty.textContent = "Nenhuma tarefa para exibir.";
    list.appendChild(empty);
    return;
  }
  tasks.forEach((task) => {
    const li = document.createElement("li");
    li.className = "list-item";
    const info = document.createElement("div");
    const title = document.createElement("strong");
    title.textContent = task.title;
    const tag = document.createElement("span");
    tag.className = "tag";
    tag.textContent = task.completed ? "Concluida" : "Pendente";
    info.appendChild(title);
    info.appendChild(tag);

    const actions = document.createElement("div");
    actions.className = "actions";

    if (!task.completed) {
      const doneBtn = document.createElement("button");
      doneBtn.className = "btn secondary";
      doneBtn.textContent = "Concluir";
      doneBtn.onclick = () => completeTask(task.id).catch((e) => toast(e.message, "error"));
      actions.appendChild(doneBtn);
    }

    const editBtn = document.createElement("button");
    editBtn.className = "btn ghost";
    editBtn.textContent = "Editar";
    editBtn.onclick = () => openModal(task);
    actions.appendChild(editBtn);

    const removeBtn = document.createElement("button");
    removeBtn.className = "btn danger";
    removeBtn.textContent = "Remover";
    removeBtn.onclick = () => removeTask(task.id).catch((e) => toast(e.message, "error"));
    actions.appendChild(removeBtn);

    li.appendChild(info);
    li.appendChild(actions);
    list.appendChild(li);
  });
}

function renderKanban(tasks) {
  const pending = el("kanbanPending");
  const completed = el("kanbanCompleted");
  pending.innerHTML = "";
  completed.innerHTML = "";
  tasks.forEach((task) => {
    const card = document.createElement("div");
    card.className = "kanban-card";
    card.draggable = true;
    card.dataset.id = task.id;
    card.dataset.completed = task.completed;
    card.textContent = task.title;
    card.addEventListener("dragstart", (event) => {
      event.dataTransfer.setData("text/plain", JSON.stringify(task));
    });
    if (task.completed) {
      completed.appendChild(card);
    } else {
      pending.appendChild(card);
    }
  });
}

function setupKanbanDrop() {
  document.querySelectorAll(".kanban-col").forEach((col) => {
    col.addEventListener("dragover", (event) => event.preventDefault());
    col.addEventListener("drop", async (event) => {
      event.preventDefault();
      const data = JSON.parse(event.dataTransfer.getData("text/plain"));
      if (col.dataset.status === "completed" && !data.completed) {
        await completeTask(data.id).catch((e) => toast(e.message, "error"));
      }
      if (col.dataset.status === "pending" && data.completed) {
        toast("Nao e possivel reabrir via drag.", "error");
      }
    });
  });
}

async function updateSummary() {
  const data = await request("/api/tasks/summary");
  el("summary").textContent = `Total: ${data.total} | Pendentes: ${data.pending} | Concluidas: ${data.done}`;
  el("kpiTotal").textContent = data.total;
  el("kpiPending").textContent = data.pending;
  el("kpiDone").textContent = data.done;
  const rate = data.total === 0 ? 0 : Math.round((data.done / data.total) * 100);
  el("completionRate").textContent = `${rate}% concluidas`;
  el("progressBar").style.width = `${rate}%`;
  el("barPending").style.setProperty("--scale", data.total ? data.pending / data.total : 0.1);
  el("barDone").style.setProperty("--scale", data.total ? data.done / data.total : 0.1);
  updateChart(data.pending, data.done);
}

function updateChart(pending, done) {
  const ctx = el("tasksChart");
  if (!ctx) return;
  if (!state.chart) {
    state.chart = new Chart(ctx, {
      type: "doughnut",
      data: {
        labels: ["Pendentes", "Concluidas"],
        datasets: [
          {
            data: [pending, done],
            backgroundColor: ["#ffd166", "#6ee7b7"],
            borderWidth: 0,
          },
        ],
      },
      options: {
        plugins: { legend: { display: false } },
      },
    });
  } else {
    state.chart.data.datasets[0].data = [pending, done];
    state.chart.update();
  }
}

async function listAll() {
  state.currentStatus = "all";
  await listPaged(0, state.currentStatus);
}

async function listPending() {
  state.currentStatus = "pending";
  await listPaged(0, state.currentStatus);
}

async function listCompleted() {
  state.currentStatus = "completed";
  await listPaged(0, state.currentStatus);
}

async function searchTasks() {
  const keyword = el("searchText").value.trim();
  if (!keyword) {
    toast("Informe a palavra-chave.", "error");
    return;
  }
  const data = await request(
    `/api/tasks/search/page?keyword=${encodeURIComponent(keyword)}&page=0&size=${state.size}&sort=id,asc`
  );
  state.tasksCache = data.items;
  renderTasks(data.items, "taskList");
  renderTasks(data.items, "taskListTasks");
  renderKanban(data.items);
  state.page = data.page;
  state.last = data.last;
  updatePager();
  await updateSummary();
}

async function clearCompleted() {
  await request("/api/tasks/clear-completed", { method: "POST" });
  toast("Concluidas removidas.");
  await listPaged(0, state.currentStatus);
}

async function listPaged(page, status = "all") {
  const data = await request(
    `/api/tasks/page?status=${status}&page=${page}&size=${state.size}&sort=completed,asc;id,asc`
  );
  state.tasksCache = data.items;
  renderTasks(data.items, "taskList");
  renderTasks(data.items, "taskListTasks");
  renderKanban(data.items);
  state.page = data.page;
  state.last = data.last;
  updatePager();
  await updateSummary();
}

function updatePager() {
  el("pageInfo").textContent = `Pagina ${state.page + 1}`;
  el("prevPageBtn").disabled = state.page <= 0;
  el("nextPageBtn").disabled = state.last;
}

function initTabs() {
  const tabs = document.querySelectorAll(".nav-item");
  tabs.forEach((btn) => {
    btn.addEventListener("click", () => {
      tabs.forEach((t) => t.classList.remove("active"));
      btn.classList.add("active");
      const target = btn.getAttribute("data-tab");
      document.querySelectorAll(".tab").forEach((tab) => {
        tab.classList.remove("active");
      });
      document.getElementById(`tab-${target}`).classList.add("active");
    });
  });
}

function toggleTheme() {
  document.body.classList.toggle("light");
  const mode = document.body.classList.contains("light") ? "light" : "dark";
  localStorage.setItem("theme", mode);
}

function loadTheme() {
  const mode = localStorage.getItem("theme") || "dark";
  if (mode === "light") {
    document.body.classList.add("light");
  }
}

function initModal() {
  el("modalCancelBtn").onclick = closeModal;
  el("modalSaveBtn").onclick = () => saveModal().catch((e) => toast(e.message, "error"));
  el("modal").addEventListener("click", (event) => {
    if (event.target.id === "modal") closeModal();
  });
}

function init() {
  el("baseUrl").value = state.baseUrl;
  setAuthStatus();
  setActiveUser(state.username);
  loadTheme();
  initTabs();
  setupKanbanDrop();
  initModal();
  recordActivity("Painel carregado.", "success");

  el("saveBaseUrl").onclick = () => {
    saveBaseUrl(el("baseUrl").value.trim());
    toast("Base URL atualizada.");
  };

  el("registerBtn").onclick = () => register().catch((e) => toast(e.message, "error"));
  el("loginBtn").onclick = () => login().catch((e) => toast(e.message, "error"));
  el("logoutBtn").onclick = logout;
  el("openAuthBtn").onclick = () => {
    document.querySelector('[data-tab="settings"]').click();
  };
  el("refreshBtn").onclick = () => listAll().catch((e) => toast(e.message, "error"));
  el("refreshTokenBtn").onclick = () => refreshToken().catch(() => {});
  el("themeToggleBtn").onclick = toggleTheme;

  el("addBtn").onclick = () => addTask("newTitle").catch((e) => toast(e.message, "error"));
  el("quickAddBtn").onclick = () => addTask("quickTitle").catch((e) => toast(e.message, "error"));
  el("listAllBtn").onclick = () => listAll().catch((e) => toast(e.message, "error"));
  el("listPendingBtn").onclick = () => listPending().catch((e) => toast(e.message, "error"));
  el("listCompletedBtn").onclick = () => listCompleted().catch((e) => toast(e.message, "error"));
  el("quickPendingBtn").onclick = () => listPending().catch((e) => toast(e.message, "error"));
  el("quickCompletedBtn").onclick = () => listCompleted().catch((e) => toast(e.message, "error"));
  el("searchBtn").onclick = () => searchTasks().catch((e) => toast(e.message, "error"));
  el("clearCompletedBtn").onclick = () => clearCompleted().catch((e) => toast(e.message, "error"));
  el("prevPageBtn").onclick = () =>
    listPaged(Math.max(0, state.page - 1), state.currentStatus || "all").catch((e) => toast(e.message, "error"));
  el("nextPageBtn").onclick = () =>
    listPaged(state.page + 1, state.currentStatus || "all").catch((e) => toast(e.message, "error"));
}

init();
