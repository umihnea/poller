import * as React from 'react';
import {useParams} from "react-router";

import UpdateForm from "../service/updateForm";
import {Link} from "react-router-dom";

const UpdateServicePage = () => {
  const params = useParams();
  return (
    <>
      <h1>Update Service</h1>
      <Link to={"/"}>Back to Dashboard</Link>
      <UpdateForm serviceId={params.serviceId} />
    </>
  );
};

export default UpdateServicePage;
