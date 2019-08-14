function getMainpanel() {
    var tabpanelMain = Ext.getCmp("faq");
    return tabpanelMain;
}
function openPanel(panel, id) {
    var mainpanel = getMainpanel();
    mainpanel.add(panel);
    mainpanel.setActiveTab(id);
}
function loadPanel(url, pre, text) {
    if (url === null || url === "") {
        return;
    }
    text=text.replace(new RegExp("\"",'g'),"“");
    var panelid = text;
    var panelname = text;
    var panel = Ext.getCmp(panelid);
    if(text.length>15){
        text = text.substring(0,13)+"..";
    }
    if (!panel) {
        panel = {
            xtype: "panel",
            id: panelid,
            name: panelname, 
            html: '<iframe frameborder=0 src="' + url + '" width="100%;" height="100%;" scrolling="yes"></iframe>',
            title: text,
            closable: true,
            closeAction:'remove',
            tooltip: panelname
        };
        openPanel(panel, panelid);
    } else {
        getMainpanel().setActiveTab(panelid);
    }
}