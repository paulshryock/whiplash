import {baseUrl} from "../config/const";
import {getUser} from "./getUser";

export const logout = async (setLoggedInState: Function) => {
    const response = await fetch(baseUrl + "user/logout", {
        method: "POST",
        mode: "same-origin",
        redirect: "error"
    });
    if (response.status != 200) {
        alert("Failed to hit server to logout");
    }
    getUser(setLoggedInState);
};

