import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';

import { DashboardPage, AddServicePage, UpdateServicePage } from './pages';
import logo from './logo.svg';
import './App.css';

function App() {
  return (
  <Router>
    <div className="App">
      <Routes>
        <Route exact path="/" element={<DashboardPage />} />
        <Route path="service">
          <Route path="add" element={<AddServicePage />} />
          <Route path="update/:serviceId" element={<UpdateServicePage />} />
        </Route>
      </Routes>
    </div>
    </Router>
  );
}

export default App;
