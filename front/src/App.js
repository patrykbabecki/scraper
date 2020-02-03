import React from 'react';
import './App.css';
import PageElements from './components/pageElements'


export class App extends React.Component {
  state = {
    pageElements: []
  };

  componentDidMount() {
    fetch("http://localhost:8080/scrape")
      .then(res => res.json())
      .then(json => this.setState({ pageElements: json }))
      .catch(console.log);
  }

  render() {
    return (
      <div>
        <PageElements pageElements={this.state.pageElements} />
      </div>
    );
  }
}

export default App;