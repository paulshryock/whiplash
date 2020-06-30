import React, {useState, useEffect, useContext, useRef} from "react";
import { useInterval } from "../common";
import "../../../resources/public/css/App.css";
import {LoginContext} from "../contexts/LoginContext";
import {CORSGetUser, OpaqueTwitchId, twitch, twitchBaseUrl} from "../TwitchExtApp";
import UIfx from 'uifx';

const { gtag } = require('ga-gtag');

const kc = new UIfx(
    (process.env.NODE_ENV === 'development' ? 'http://localhost:3000/' : "https://whiplashesports.com/") + "dist/sfx/ka-ching.mp3",
    {
        volume: 0.4, // number between 0.0 ~ 1.0
        throttleMs: 100
    }
)

const textStyles = {fontSize: ".75rem", padding: "0 0.5rem 0.5rem 0.5rem",}

export function TwitchExtension(props: any) {
    const { loggedInState, setLoggedInState } = useContext(LoginContext);
    const [proposition, setProposition] = useState<any>({});
    const [prevProposition, setPrevProposition] = useState<any>({});
    const [extClass, setExtClass] = useState<string>("twitch-extension slide-out");
    const [extPositionClass, setExtPositionClass] = useState<string>("");

    const [betAmount, setBetAmount] = useState<number>(0);
    const [betWaitingForResp, setBetWaitingForResp] = useState<boolean>(false);
    // @ts-ignore
    const pulseP = useRef(null);

    useEffect(() => {
        const buttons = document.querySelectorAll('.button--vote');

        buttons.forEach((button) => {
            if (!toggleValid()) {
                // button text does match selection
                if (!button.classList.contains('is-active')) {
                    button.classList.add('is-active')
                }
            } else {
                // button text does not match selection
                button.classList.remove('is-active')
            }
        })
    }, [betAmount, loggedInState.cash]);

    //Play a ding if any notifications are a payout (this include cancels here)
    useEffect(() => {
        if (loggedInState.notifications.length > 0) {
            let i = 0;
            for (; i < loggedInState.notifications.length; i++) {
                // @ts-ignore
                if (loggedInState.notifications[i]["notification/type"] === "notification.type/payout") {
                    kc.play()
                    break;
                }
            }
        }
    }, [loggedInState.notifications])

    const CORSMakePropBet = async (projectedResult : boolean) => {
        setBetWaitingForResp(true);
        const response = await fetch(twitchBaseUrl + "user/prop-bet", {
            headers: {
                "Content-Type": "application/json",
                "x-twitch-opaque-id": OpaqueTwitchId,
            },
            method: "POST",
            credentials: "omit",
            mode: "cors",
            redirect: "error",
            body: JSON.stringify({
                projected_result: projectedResult,
                bet_amount: betAmount,
            })
        });

        // Trigger Google Analytics event
        gtag('event', 'prop-bet', {
            event_category: 'Betting',
            event_label: loggedInState.userName,
            value: betAmount
        });

        setBetWaitingForResp(false);
        if (response.status === 200) {
            // update user's cash
            setLoggedInState(
                { userName: loggedInState.userName,
                    status: loggedInState.status,
                    cash: loggedInState.cash - betAmount,
                    notifications: loggedInState.notifications})
        }
    };

    const getCORSProp = async () => {
        const response = await fetch(twitchBaseUrl + "stream/prop", {
            method: "GET",
            credentials: "omit",
            mode: "cors",
            redirect: "error",
        });
        if (response.status === 200) {
            return await response.json();
        } else {
            return {}
        }
    };

    const getPropWrapper = (event:any) => {
        if (event["current-prop"]){
            setProposition(event["current-prop"]);
        } else {
            setProposition({});
        }
        if (event["previous-prop"]){
            setPrevProposition(event["previous-prop"]);
        } else {
            setPrevProposition({});
        }
    };

    useEffect(() => {
        getCORSProp().then((event) => {
            getPropWrapper(event)
        });

        twitch.configuration.onChanged(() => {
            let config = twitch.configuration.broadcaster ? twitch.configuration.broadcaster.content : "";

            try {
                config = JSON.parse(config)
            } catch (e) {
                config = ""
            }
            setExtPositionClass(config === "bottomleft" ? "is-bottom-left" : "");
        })

    }, []);

    useEffect( () => {
        if (Object.keys(proposition).length === 0 && proposition.constructor === Object) {
            setExtClass("twitch-extension slide-out " + extPositionClass)
        } else if (proposition["proposition/text"]) {
            setExtClass("twitch-extension slide-in " + extPositionClass)
        }
    }, [proposition])

    useInterval(() => {
        // @ts-ignore
        pulseP.current?.classList.remove("profit-pulse")
        CORSGetUser(loggedInState, setLoggedInState).then((delta) => {
                if (delta > 0) {
                    // apply animation to show cash increase
                    // @ts-ignore
                    pulseP.current?.classList.add("profit-pulse")
                }
            }
        );
    }, 5000);

    useInterval(() => {
        getCORSProp().then((event) => {
            getPropWrapper(event)
        });
    }, 1000);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const numbers = /^[0-9]*$/;
        if (numbers.test(e.target.value)) {
            const amount = parseInt(e.target.value, 10) || 0;
            setBetAmount(amount);
        }
    };

    const toggleValid = () => {
        if (loggedInState.status === "user.status/twitch-ext-unauth")
        {
            return betAmount === 0 ||
                betAmount > loggedInState.cash ||
                betWaitingForResp;

        } else {
            return betAmount === 0 ||
                // TODO: change this to a constant
                betAmount > 500 ||
                betWaitingForResp;
        }
    };

    const renderOutcomeText = (result: string) => {
        if (result === "proposition.result/true" ) {
            return "Yes";
        } else if (result === "proposition.result/false") {
            return "No";
        } else {
            return "Cancelled"
        }
    };

    const renderPropositionText = () => {
        if (proposition["proposition/text"]) {
            return (<p style={{margin:0}}>Bet: {proposition["proposition/text"]}</p>);
        } else if (prevProposition["proposition/text"]){
            return (
                <>
                    <p>{"Last bet: " + prevProposition["proposition/text"]}</p>
                    <p style={{margin:0}}>
                        {"Outcome: " + renderOutcomeText(prevProposition["proposition/result"])}
                    </p>
                </>
            );
        } else {
            return <p>Next proposition soon!</p>;
        }
    };

    const renderBody = () => {
        if (proposition["proposition/betting-seconds-left"] > 0 && loggedInState.cash !== 0) {
            return (
                <>
                    <p style={{...textStyles, ...{margin: 0}}}>
                        Countdown: {proposition["proposition/betting-seconds-left"]}
                    </p>
                    <div className="form__group">
                        <input
                            style={{margin: "0.5rem"}}
                            className="form__input"
                            value={betAmount > 0 ? betAmount : ""}
                            onChange={e => handleInputChange(e)}
                            type="text"
                            name="betAmount"
                            id="betAmount"
                            autoComplete="off"
                            placeholder="W$100 minimum"
                        />
                    </div>
                    <div style={{display: "flex", marginBottom: 0, justifyContent: "space-evenly"}}>
                        <button
                            className="button button--vote "
                            style={{minWidth: "40%", margin: "0.25em", padding: "0.25rem"}}
                            type="button"
                            key="Yes"
                            disabled={toggleValid()}
                            onClick={() => {
                                CORSMakePropBet(true)
                            }}>
                            <div className={betWaitingForResp ? "loading" : ""}>
                                {betWaitingForResp ? "" : "Yes"}
                            </div>
                        </button>
                        <button
                            className="button button--vote"
                            style={{minWidth: "40%", margin: "0.25em", padding: "0.25rem"}}
                            type="button"
                            key="No"
                            disabled={toggleValid()}
                            onClick={() => {
                                CORSMakePropBet(false)
                            }}>
                            <div className={betWaitingForResp ? "loading" : ""}>
                                {betWaitingForResp ? "" : "No"}
                            </div>
                        </button>
                    </div>
                </>
            );
        } else if (loggedInState.cash === 0){
            return (
                <div style={{padding: "0 0.5rem 0.5rem 0.5rem"}}>Sign up on Whiplash.gg to keep playing!</div>
            );
        }
    }

    const renderContent = () => {
        // @ts-ignore
        return (
            // TODO: uninline styles
            <div className={extClass}>
                <span style={{display: "flex", justifyContent: "space-between"}}>
                    <img
                        src={twitchBaseUrl + "/img/favicon/favicon-228.png"}
                        alt="Whiplash"
                        width="45"
                        height="45"
                        // className="site-logo"
                        style={{margin:"0.5rem"}}
                    />
                    <p style={{...textStyles, ...{ padding: "0.5rem", margin: 0}}}>
                    {/*Show 500 if cash is default. Cash gets set by getUser but only after someone has placed a bet.*/}
                        W$: <span ref={pulseP} style={{color: "gold"}}>{loggedInState.cash === -1 ? 500 : loggedInState.cash}</span>
                    </p>
                </span>
                <div style={{...textStyles, ...{fontSize: "1rem"}}}>
                    {renderPropositionText()}
                </div>
                {renderBody()}
            </div>
        );
    };

    return (
        <>
            {renderContent()}
        </>
    );
}
