import React from "react";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {inject, observer} from "mobx-react";

import {withStyles} from "@material-ui/core/styles";
import {Box, CssBaseline} from "@material-ui/core";
import { styles } from './AppStyles';

import TopBar from "./components/TopBar";
import SideMenu from "./components/SideMenu";
import ScrollToTop from "./components/ScrollToTop";
import Home from "./views/Home";
import SignIn from "./views/SignIn";
import * as store from "./stores/AuthStore";

class App extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            mobileOpen: false,
            menuOpen: true,
        };

        this.setMobileOpen = this.setMobileOpen.bind(this);
    }

    componentDidMount() {
        this.props.authStore.checkLogin();
    }

    setMobileOpen(mobileOpen) {
        this.setState({mobileOpen: mobileOpen});
    }

    handleDrawerToggle = () =>  {
        this.setState({menuOpen: !this.state.menuOpen});
    }

    render() {
        const { classes } = this.props;
        const { loginState, loginUser } = this.props.authStore;

        return (
            <Box className={classes.root} display="flex" flexDirection="row" justifyContent="center" alignItems="stretch">
                <Router>
                    <CssBaseline />

                    <Route path="/" component={ScrollToTop}>
                        <TopBar mobileOpen={this.state.mobileOpen}
                                menuOpen={this.state.menuOpen}
                                setMobileOpen={this.setMobileOpen}
                                user={loginUser}
                                isLoggedIn={loginState === store.State.Authenticated}
                                doLogout={() => this.props.authStore.doLogout()} />
                        <SideMenu handleDrawerToggle={this.handleDrawerToggle}
                                  menuOpen={this.state.menuOpen}
                                  user={loginUser}
                                  isLoggedIn={loginState === store.State.Authenticated} />

                        {loginState === store.State.Authenticated ? (
                            <React.Fragment>
                              <Switch>
                                <Route exact path="/" component={Home} />
                                <Route exact path="/home" component={Home} />
                              </Switch>
                            </React.Fragment>
                        ) : (
                            <Route path="/" component={SignIn} />
                        )}
                  </Route>
                </Router>
            </Box>
        );
    }
};

export default withStyles(styles) (
    inject('authStore') (
        observer(App)
    )
);

