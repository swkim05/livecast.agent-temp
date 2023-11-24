import {Repository} from "./Repository";

export default class AuthRepository extends Repository {
    constructor(props) {
        super();

        this.requestPrefix = props.serverContextPath + "/api/v1/authentications";
    }

    signIn = (param) => {
        return new Promise((resolve, reject) => {
            this.getRequestPromise('post', this.requestPrefix + '/signin', param)
                .then(data => {
                    const token = data.token;
                    this.setAuthTokenToStorage(token);

                    resolve(data.user);
                })
                .catch(error => {
                    this.removeAuthTokenFromStorage();

                    reject(error);
                });
        });
    }

    signCheck = () => {
        return new Promise((resolve, reject) => {
            this.getRequestPromise('get', this.requestPrefix + '/signcheck')
                .then(data => {
                    resolve(data);
                })
                .catch(error => {
                    this.removeAuthTokenFromStorage()

                    reject(error);
                });
        });
    }

    signOut = () => {
        return new Promise((resolve, reject) => {
            this.getRequestPromise('post', this.requestPrefix + '/signout')
                .then(data => {
                    this.removeAuthTokenFromStorage();

                    resolve();
                })
                .catch(error => {
                    this.removeAuthTokenFromStorage();

                    reject(error);
                });
        });
    }
}