import * as React from 'react';
import {Link} from "react-router-dom";

import AddForm from "../service/addForm";

const AddServicePage = () => {
  return (
    <>
      <h1>Add Service</h1>
      <Link to={"/"}>Back to Dashboard</Link>
      <AddForm />
    </>
  );
};

export default AddServicePage;
