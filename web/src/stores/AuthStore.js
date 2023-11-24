import {makeAutoObservable} from "mobx";

export const State = {
    Authenticated: 'Authenticated',
    NotAuthenticated: 'NotAuthenticated',
    Pending: 'Pending',
    Failed: 'Failed',
};

export const LocalStorageTokenKey = '_BASKITOP_AUTHENTICATION_TOKEN_';

const EmptyLogin = {
    id: '',
    password: '',
};

const EmptyUser = {
    id: '',
    name: '',
    type: '',
    createdDatetime: '',
    updatedDatetime: '',
};

export default class AuthStore {
    constructor(props) {
        this.authRepository = props.authRepository;

        // this.authState = AuthState.None;
        // this.user = undefined;

        makeAutoObservable(this);
    }

    login = Object.assign({}, EmptyLogin);
    loginState = State.NotAuthenticated;
    loginUser = Object.assign({}, EmptyUser);

    changeLoginId = (id) => {
        this.login.id = id;
    };

    changeLoginPassword = (password) => {
        this.login.password = password;
    };

    invalidateLogin = () => {
        this.login = Object.assign({}, EmptyLogin);
        this.loginState = State.NotAuthenticated;
        this.loginUser = Object.assign({}, EmptyUser);
    };

    *doLogin() {
        this.loginState = State.Pending;

        try {
            const param = this.login;
            const user = yield  this.authRepository.signIn(param);

            this.loginState = State.Authenticated;
            this.loginUser = user;
        } catch (e) {
            this.loginState = State.Failed;
            this.loginToken = '';
            this.loginUser = Object.assign({}, EmptyUser);
        }
    }

    *checkLogin() {
        const token = localStorage.getItem(LocalStorageTokenKey);

        if(token) {
            try {
                const user = yield  this.authRepository.signCheck();

                this.loginState = State.Authenticated;
                this.loginUser = user;
            } catch(e) {
                this.loginState = State.NotAuthenticated;
                this.loginUser = Object.assign({}, EmptyUser);
            }
        }
    }

    *doLogout() {
        localStorage.removeItem(LocalStorageTokenKey);

        try {
            yield this.authRepository.signOut();

            this.login = Object.assign({}, EmptyLogin);
            this.loginState = State.NotAuthenticated;
            this.loginUser = Object.assign({}, EmptyUser);
        } catch(e) {
            this.login = Object.assign({}, EmptyLogin);
            this.loginState = State.NotAuthenticated;
            this.loginUser = Object.assign({}, EmptyUser);
        }
    }
}