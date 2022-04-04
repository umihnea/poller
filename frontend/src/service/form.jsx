import * as React from "react";

const Form = ({onSubmit}) => {
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
      <input type="text" name="name" ref={nameRef}/>
    </label>
    <label>
      Service URL:
      <input type="text" name="url" ref={urlRef}/>
    </label>
    <br/>
    <button type="submit">Submit</button>
  </form>);
};

export default Form;
