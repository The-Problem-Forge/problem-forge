import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authAPI = {
  login: (login, password) =>  // parameter renamed
    api.post('/auth/login', { login, password }), // field names updated
  
  register: (login, password) =>  // parameter renamed
    api.post('/auth/register', { login, password }), // field names updated
};

export const taskAPI = {
  getTasks: () => api.get('/tasks'),
  createTask: (task) => api.post('/tasks', task),
};

export default api;