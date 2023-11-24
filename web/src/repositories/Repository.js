import axios from "axios";

export const AuthTokenStorageKey = '__OTL_Authentication_Token__';

// const LogPrefix = '[Repository]';
export class Repository {
    getRequestPromise = (method, url, data, contentType) => {
        const token = sessionStorage.getItem(AuthTokenStorageKey);
        const headers = Boolean(token) ? {'X-Auth-Token': token, 'Content-Type': (contentType ? contentType : 'application/json')} : {};

        return new Promise((resolve, reject) => {
            const config = {
                method: method,
                url: url,
                headers: headers,
                data: data,
            };

            // console.log(LogPrefix, 'HTTP requesting :', config);
            axios.request(config)
                .then(response => {
                    resolve(response.data);
                })
                .catch(error => {
                    reject(error);
                });
        });
    }

    getAuthTokenFromStorage = () => {
        return sessionStorage.getItem(AuthTokenStorageKey);
    }

    setAuthTokenToStorage = (token) => {
        sessionStorage.setItem(AuthTokenStorageKey, token);
    }

    removeAuthTokenFromStorage = () => {
        sessionStorage.removeItem(AuthTokenStorageKey);
    }
}