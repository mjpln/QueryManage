//if  (!Ext.grid.GridView.prototype.templates) {      
//    Ext.grid.GridView.prototype.templates = {};      
//}      
//Ext.grid.GridView.prototype.templates.cell =  new  Ext.Template(      
//     '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>' ,      
//     '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>' ,      
//     '</td>'      
//);
Ext.view.TableChunker.metaRowTpl = [
            '<tr class="' + Ext.baseCSSPrefix + 'grid-row {addlSelector} {[this.embedRowCls()]}" {[this.embedRowAttr()]}>',
            '<tpl for="columns">',
            '<td class="{cls} ' + Ext.baseCSSPrefix + 'grid-cell ' + Ext.baseCSSPrefix + 'grid-cell-{columnId} {{id}-modified} {{id}-tdCls} {[this.firstOrLastCls(xindex, xcount)]}" {{id}-tdAttr}><div class="' + Ext.baseCSSPrefix + 'grid-cell-inner" style="{{id}-style}; text-align: {align};">{{id}}</div></td>',
            '</tpl>',
            '</tr>'
        ];
Ext.core.Element.prototype.unselectable = function () {
    var me = this;
    if (me.dom.className.match(/(x-grid-table|x-grid-view)/)) {
        return me;
    }
    me.dom.unselectable = "on";
    me.swallowEvent("selectstart", true);
    me.applyStyles("-moz-user-select:none;-khtml-user-select:none;");
    me.addCls(Ext.baseCSSPrefix + 'unselectable');
    return me;
};