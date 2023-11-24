import XCircle from "../../../../common/images/XCircle.svg";

export const styles = () => ({
    mainContainer: {
        flexGrow: 1,
        padding: '0 24px 56px',
        background: '#f1f1f1',
        '& *':{
            fontFamily: 'Noto Sans KR'
        }
    },
    mainContent: {
        marginTop: 80,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    titleBox:{
        width: '100%',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingBottom: 5,
        paddingTop: 40,
        boxSizing:'border-box',
        borderBottom: '1px solid #d9d9d9'
    },
    titleText:{
        fontSize: '1.5rem',
        color: '#333',
        fontWeight: 500
    },
    topButton:{
        width: 56,
        height: 34,
        background: '#fff',
        border: '1px solid rgba(0, 0, 0, 0.3)',
        borderRadius: 5,
        boxShadow: 'none',
        fontSize: '0.938rem',
        color: '#333',
        fontWeight: 500,
        '&:hover':{
            background: '#fff',
            boxShadow: 'none',
        }
    },
    contentBox:{
        width: '100%',
        background: '#fff',
        marginTop: 40,
        paddingTop: 10,
        paddingBottom: 40,
        boxSizing: 'border-box',
        boxShadow: '0 2px 4px 0 rgba(0, 0, 0, 0.25)'
    },
    contentPadding:{
        width: '100%',
        padding: '0 30px',
        boxSizing: 'border-box'
    },
    labelText: {
        fontSize: '0.938rem',
        color: 'rgba(102, 102, 102, 0.8)',
        fontWeight: 500,
        marginRight: 6
    },
    labelWidth:{
        width: 270,
        marginRight: 0
    },
    inputText: {
        fontSize: '0.875rem',
        color: '#333',
        marginRight: 20
    },
    numberText:{
        fontSize: '0.875rem',
        fontFamily: 'Roboto'
    },
    textStyle:{
        fontSize: '0.938rem',
        fontWeight: 500,
        color: '#333333'
    },
    radioBox:{
        '& .MuiFormControlLabel-label':{
            fontSize: '0.875rem',
            fontWeight: 500,
            color: 'rgba(102, 102, 102, 0.9)'
        }
    },
    saveButton:{
        height: 54,
        padding: '0 80px',
        boxSizing: 'border-box',
        borderRadius: 7,
        background: '#ff404b',
        boxShadow: 'none',
        fontSize: '1.313rem',
        fontWeight: 'bold',
        color: '#fff',
        '&:hover':{
            background: '#ff404b',
            boxShadow: 'none',
        }
    },
    buttonStyle2:{
        border: '1px solid #dadada',
        borderRadius: 5,
        minWidth: 40,
        width: 40,
        height: 30,
        padding: 0,
        fontSize: '0.813rem',
        color: '#333',
        background: '#fff',
        boxShadow:'none',
        '&:hover':{
            background: 'transparent',
            boxShadow:'none',
        }
    },
    inputBox:{
        border: '1px solid #dadada',
        borderRadius: 5,
        padding: '0 0 0 5px',
        marginRight: 14,
        height: 30,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        '& input':{
            outline: 'none',
            border: 0,
            fontSize: '0.875rem',
            color: '#333',
        },
        '& input[type="search"]::-webkit-search-cancel-button':{
            WebkitAppearance: 'none',
            height: 20,
            width: 20,
            borderRadius: '50%',
            background: `url(${XCircle}) no-repeat 50% 50%`,
            backgroundSize: 'contain',
            opacity: 0,
            pointerEvents: 'none',
        },
        '& input[type="search"]:focus::-webkit-search-cancel-button':{
            opacity: 1,
            pointerEvents: 'all'
        }
    },
    errorText: {
        fontSize: '0.813rem',
        color: '#ff0000',
        margin: '4px 0 0',
    },
});