import React, { useState } from 'react';
import { authAPI } from '../services/api';

const Login = ({ onLogin }) => {
  const [login, setLogin] = useState(''); // state renamed
  const [password, setPassword] = useState('');
  const [isRegister, setIsRegister] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      const response = isRegister 
        ? await authAPI.register(login, password) // parameter renamed
        : await authAPI.login(login, password); // parameter renamed
      
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('login', response.data.login); // key renamed
      onLogin(response.data.login); // parameter renamed
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed');
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px' }}>
      <h2>{isRegister ? 'Register' : 'Login'}</h2>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '15px' }}>
          <input
            type="text"
            placeholder="Login" // placeholder renamed
            value={login} // value renamed
            onChange={(e) => setLogin(e.target.value)} // onChange renamed
            required
            style={{ width: '100%', padding: '8px' }}
          />
        </div>
        <div style={{ marginBottom: '15px' }}>
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ width: '100%', padding: '8px' }}
          />
        </div>
        {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}
        <button type="submit" style={{ width: '100%', padding: '10px' }}>
          {isRegister ? 'Register' : 'Login'}
        </button>
      </form>
      <button 
        onClick={() => setIsRegister(!isRegister)}
        style={{ width: '100%', marginTop: '10px', padding: '10px' }}
      >
        {isRegister ? 'Already have an account? Login' : 'Need an account? Register'}
      </button>
    </div>
  );
};

export default Login;