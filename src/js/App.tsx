import React, { useEffect, useState, lazy, Suspense} from "react";
import "../../resources/public/css/App.css";
import { BrowserRouter, Route } from "react-router-dom";
import { Home } from "./components/Home";
import { About } from "./components/About";
import { Verify } from "./components/Verify";
import {defaultLoggedIn, LoginContext} from "./contexts/LoginContext";
import { Account } from "./components/Account";
import {getUser} from "./common/getUser";
import {Recovery} from "./components/Recovery";
import {LeaderboardPage} from "./components/LeaderboardPage";
import {identify} from "./common/fullstory";

const { install } = require('ga-gtag');
const Control = lazy(() => import("./components/Control").then(({ Control }) => ({default: Control})));

export const App = () => {
    const [loggedInState, setLoggedInState] = useState(defaultLoggedIn);

    useEffect(() => {
        // Install Google tag manager, will only track on hostnames that contain 'whiplash'
        // this was configured as a filter named 'whiplash hostname filter (filter out local host)'
        // in the GA admin panel
        install('UA-154430212-2');

        getUser(setLoggedInState).then( (status) => {
            if (status === 200) {
                identify(loggedInState);
            }
        });
    }, []);

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
                <Route exact path="/leaderboard" render={({ match, history}) =>
                    (
                        <LeaderboardPage match={match} history={history}/>
                    )}
                />
                <Route
                    path="/user/verify"
                    render={({ match, history, location }) => (
                        <Verify match={match} history={history} location={location}/>
                    )}
                />
                <Route
                    path="/user/password/recover"
                    render={({ match, history, location }) => (
                        <Recovery match={match} history={history} location={location}/>
                    )}
                />
                <Route exact path="/control" render={({ match, history}) =>
                    (
                        <Suspense fallback={<div>Loading...</div>}>
                            <Control match={match} history={history}/>
                        </Suspense>
                    )}
                />
            </BrowserRouter>
            </LoginContext.Provider>
    );
};
