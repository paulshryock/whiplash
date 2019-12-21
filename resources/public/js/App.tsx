import React, {useContext, useEffect, useState} from "react";
import { BrowserRouter, Route } from "react-router-dom";
import { Home } from "./components/Home";
import { About } from "./components/About";
import { Verify } from "./components/Verify";
import {defaultLoggedIn, LoginContext} from "./contexts/LoginContext";
import { Account } from "./components/Account";
import {useInterval} from "./common";
import {getUser} from "./common/getUser";
import {match} from "react-router";

export const App = () => {
    const [loggedInState, setLoggedInState] = useState(defaultLoggedIn);

    useEffect(() => {
        getUser(setLoggedInState)
    }, []);

    useInterval(() => {
        if (loggedInState.userName) {
            getUser(setLoggedInState);
        }
    }, 10000);

    return (
        <LoginContext.Provider value={{loggedInState: loggedInState, setLoggedInState: setLoggedInState}}>
            <BrowserRouter>
                <Route exact path="/" component={Home} />
                <Route exact path="/about" component={About} />
                <Route exact path="/account" render={({ match, history}) =>
                    (
                        <Account match={match} history={history}/>
                    )}
                />
                {/* <Route
          path="/signup"
          render={({ match, history }) => (
            <Signup match={match} history={history} />
          )}
        /> */}
                <Route
                    path="/user/verify"
                    render={({ match, history, location }) => (
                        <Verify match={match} history={history} location={location}/>
                    )}
                />
            </BrowserRouter>
            </LoginContext.Provider>
    );
};
