import React from 'react';
import events from './events.json';


const { createContext, useContext, useReducer } = React;

const initialState = {
  showAddGame: false,
  showViewGame: false,
  showEditGame: false,
  clickedGame: []
};

type State = typeof initialState;

type Action =
  | { type: 'ADD_GAME' }
  | { type: 'CLOSE_ADD_GAME' }
  | { type: 'VIEW_GAME', payload: any }
  | { type: 'CLOSE_VIEW_GAME' }
  | { type: 'EDIT_GAME' }
  | { type: 'CLOSE_EDIT_GAME' }

const reducer = (state: State, action: Action) => {
  switch (action.type) {
    case 'ADD_GAME': return {
      showAddGame: true,
      showViewGame: false,
      showEditGame: false,
      clickedGame: []
    };
    case 'CLOSE_ADD_GAME': return {
      showAddGame: false,
      showViewGame: false,
      showEditGame: false,
      clickedGame: []
    }
    case 'VIEW_GAME': return {
      showAddGame: false,
      showViewGame: true,
      showEditGame: false,
      clickedGame: action.payload
    }
    case 'CLOSE_VIEW_GAME': return {
      showAddGame: false,
      showViewGame: false,
      showEditGame: false,
      clickedGame: []
    }
    case 'EDIT_GAME': return {
      showAddGame: false,
      showViewGame: false,
      showEditGame: true,
      clickedGame: []
    }
    case 'CLOSE_EDIT_GAME': return {
      showAddGame: false,
      showViewGame: false,
      showEditGame: false,
      clickedGame: []
    }
    default: return state;
  }
};

const stateCtx = createContext(initialState);
const dispatchCtx = createContext((() => 0) as React.Dispatch<Action>);

export const Provider: React.ComponentType = ({ children }) => {
    const [state, dispatch] = useReducer(reducer, initialState);
    return (
      <dispatchCtx.Provider value={dispatch}>
        <stateCtx.Provider value={state}>
          {children}
        </stateCtx.Provider>
      </dispatchCtx.Provider>
    );
  };

  export const useDispatch = () => {
    return useContext(dispatchCtx);
  };
  
  export const useGlobalState = <K extends keyof State>(property: K) => {
    const state = useContext(stateCtx);
    return state[property]; // only one depth selector for comparison
  };

  export const getGames = () => {
    return events.events;
  }