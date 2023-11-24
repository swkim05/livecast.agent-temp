export const styles = (theme) => ({
    root:{
        height:'100vh',
        display: 'flex',
        alignItems: 'center',
        '& *':{
            fontFamily:'Noto Sans KR',
        },
        '& .MuiContainer-root':{
            padding:'58px 100px',
            border:'1px solid #d9d9d9',
            borderRadius:12,
        },
    },
    appBarSpacer: theme.mixins.toolbar,
    paper: {
        // marginTop: theme.spacing(8),
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    titleStyle:{
        marginBottom: theme.spacing(1),
        color:'#333',
        fontSize:'1.75rem',
        fontWeight:600,
    },
    lockOpenAvatar: {
        margin: theme.spacing(1),
        backgroundColor: theme.palette.primary.main,
    },
    lockOutAvatar: {
        margin: theme.spacing(1),
        backgroundColor: theme.palette.secondary.main,
    },
    form: {
        width: '100%',
        // marginTop: theme.spacing(2),
    },
    inputStyle:{
        color:'#B2B2B2',
        fontSize: '1rem',
        marginTop: 20,
        '& .MuiOutlinedInput-notchedOutline, .MuiOutlinedInput-root:hover .MuiOutlinedInput-notchedOutline':{
            borderColor:'#d9d9d9',
            borderRadius: 8,
        },
        '& .MuiOutlinedInput-root.Mui-focused .MuiOutlinedInput-notchedOutline':{
            borderColor:'#ff404b',
        },
    },
    checkBoxStyle: {
        display: 'flex',
        alignItems: 'center',
        fontSize: '0.875rem',
        fontWeight: 300,
        marginTop: 10,
        cursor: 'pointer',
        '& svg': {
            width: 14,
            height: 14,
            marginRight: 4,
        },
    },
    checkBoxStyleOn: {
        // color: '#0097FF',
    },
    userStyle: {
        color: '#a3a8af',
        fontSize: '0.813rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        '& svg': {
            marginRight: 4,
        },
    },
    submit: {
        height:56,
        margin: theme.spacing(3, 0,3),
        backgroundColor:'#ff404b',
        borderRadius:5,
        color:'#fff',
        fontSize:'1.125rem',
        fontWeight:600,
        boxShadow:'none',
        padding:'15px 18px',
        '&:hover':{
            background:'#ff404b',
            boxShadow: 'none',
        }
    },
    linkStyle:{
        fontSize:'0.875rem',
        textDecoration:'underline',
        color:'#333',
        cursor : 'pointer',
        fontWeight:400,
    },
    infoStyle:{
        paddingTop:20,
        borderTop:'1px dotted #d9d9d9',
        color:'#666',
        fontSize:'0.813rem',
        fontWeight:200,
    }
});