import * as React from 'react';
import {Link} from 'react-router-dom';

import {ServiceList} from '../service';

const DashboardPage = () => {
  return (
    <>
      <h1>Monitor Dashboard</h1>
      <Link to={"/service/add"}>Add</Link>
      <ServiceList/>
    </>
  );
};

export default DashboardPage;
