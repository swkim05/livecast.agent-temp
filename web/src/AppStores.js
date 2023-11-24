import {serverContextPath} from "./AppConstants";
import AuthRepository from "./repositories/AuthRepository";


import AuthStore from "./stores/AuthStore";

const repositoryProps = {
    serverContextPath: serverContextPath,
};

const authRepository = new AuthRepository(repositoryProps);
const storeProps = {
};

export const stores = {
    authStore: new AuthStore({authRepository, ...storeProps})
};
