import * as React from "react";

const defaultValue = (data, fieldName) => {
  const value = data?.[fieldName];
  if (!value) {
    console.warn(`loadField: '${fieldName}' is null.`)
    return;
  }

  return value;
};

const Form = ({onSubmit, loadedData}) => {
  const nameRef = React.createRef();
  const urlRef = React.createRef();

  const wrappedOnSubmit = React.useCallback(event => {
    event.preventDefault();
    const payload = {
      name: nameRef.current.value,
      url: urlRef.current.value,
    };
    onSubmit(payload);
  }, [nameRef, onSubmit, urlRef]);

  return (<form onSubmit={wrappedOnSubmit}>
    <label>
      Service Name:
      <input type="text" name="name" ref={nameRef} defaultValue={defaultValue(loadedData, "name")}/>
    </label>
    <label>
      Service URL:
      <input type="text" name="url" ref={urlRef} defaultValue={defaultValue(loadedData, "url")}/>
    </label>
    <br/>
    <button type="submit">Submit</button>
  </form>);
};

export default Form;
