import React from "react";
import {Link} from "react-router-dom";

import {makeStyles} from "@material-ui/core/styles";
import {AppBar, Box, IconButton, Toolbar} from "@material-ui/core";
import MenuIcon from '@material-ui/icons/Menu';
import ExitToAppIcon from "@material-ui/icons/ExitToApp";
import OnTheLiveLogo from "../common/images/onthelive_logo.svg";
import clsx from "clsx";

const logoWidth = 120;

const useStyles = makeStyles((theme) => ({
    appBar: {
        display:'flex',
        justifyContent:'center',
        backgroundColor:'#fff',
        color:'#333',
        [theme.breakpoints.up('sm')]: {
            width:'100%',
        },
        '& *':{
            fontFamily:'Noto Sans KR',
        },
        '& img':{
            height: 35
        }
    },
    appBarLogin:{
        alignItems:'flex-end',
        width: `calc(100% - ${theme.drawerWidth}px)`,
        marginLeft: theme.drawerWidth,
    },
    appBarLoginMenuClose:{
        alignItems:'flex-end',
        width: `calc(100% - 68px)`,
        marginLeft: 68,
    },
    menuButton: {
        marginRight: theme.spacing(2),
        [theme.breakpoints.up('sm')]: {
            display: 'none',
        },
    },
    title: {
        marginLeft: (theme.sideMenuWidth - logoWidth) / 2,
        paddingLeft: theme.spacing(3),
        flexGrow: 1,
    },
    link: {
        textDecoration: 'none',
        color: 'inherit',
    },
    userInfoStyle:{
        display:'flex',
        alignItems:'center',
        marginRight:40,
        fontSize:'1rem',
        color:'#333',
        letterSpacing:'-0.32px',
        '& > p:first-child':{
            marginRight:9,
            paddingRight:10,
            borderRight:'1px solid #d8d8d8',
            lineHeight:1,
        }
    },
    iconBtnStyle:{
        '&:hover':{
            background:'transparent',
        }
    },
}));

export default function TopBar(props) {
    const classes = useStyles();
    const { mobileOpen, setMobileOpen, isLoggedIn, doLogout, menuOpen} = props;

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    return (
        <AppBar position="fixed" className={isLoggedIn ? menuOpen ? clsx(classes.appBar, classes.appBarLogin) : clsx(classes.appBar, classes.appBarLoginMenuClose) : classes.appBar}>
            <Toolbar>
                {!isLoggedIn ? (
                    <Link to='/'>
                        <img src={OnTheLiveLogo} alt="Onthelive" />
                    </Link>
                ) : (
                    ''
                )}
                <IconButton
                    color="inherit"
                    aria-label="open drawer"
                    edge="start"
                    onClick={handleDrawerToggle}
                    className={classes.menuButton}
                >
                    <MenuIcon />
                </IconButton>
                {/*<Typography variant="h6" noWrap className={classes.title}>*/}
                {/*    <Link to='/' className={classes.link}>*/}
                {/*        Project Base*/}
                {/*    </Link>*/}
                {/*</Typography>*/}

                {isLoggedIn ? (
                    <Box display='flex'>
                        <Box className={classes.userInfoStyle}>
                            <IconButton onClick={doLogout} className={classes.iconBtnStyle}>
                                <ExitToAppIcon />
                            </IconButton>
                        </Box>
                    </Box>
                ) : (
                    ''
                )}
            </Toolbar>
        </AppBar>
    );
}