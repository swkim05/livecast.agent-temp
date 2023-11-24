export const styles = (theme) => ({
    root: {
        width: '100%',
        height: '100%',
        // overflowX: 'hidden',
        // overflowY: 'auto',
    },
    mobileRoot:{
        width: '100%',
        // height: '100%',
    },
    headerBox: {
        minHeight: theme.mixins.toolbar.minHeight,
        backgroundColor: theme.palette.primary.main,
    },
    headerText: {
        color: '#fff',
    },
});