/*
 * File: app/store/faqStore.js
 *
 * This file was generated by Sencha Architect version 3.0.4.
 * http://www.sencha.com/products/architect/
 *
 * This file requires use of the Ext JS 4.2.x library, under independent license.
 * License of Sencha Architect does not include license for Ext JS 4.2.x. For more
 * details see http://www.sencha.com/license or contact license@sencha.com.
 *
 * This file will be auto-generated each and everytime you save your project.
 *
 * Do NOT hand edit this file.
 */

Ext.define('MyApp.store.faqStore', {
    extend: 'Ext.data.Store',

    requires: [
        'MyApp.model.faqModel',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json'
    ],

    constructor: function(cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            model: 'MyApp.model.faqModel',
            storeId: 'faqStore',
            pageSize: 10,
            proxy: {
                type: 'ajax',
                url: '../faq.action',
                reader: {
                    type: 'json',
                    root: 'root'
                }
            },
            listeners: {
                beforeload: {
                    fn: me.onJsonstoreBeforeLoad,
                    scope: me
                }
            }
        }, cfg)]);
    },

    onJsonstoreBeforeLoad: function(store, operation, eOpts) {
        var param = {};
        param["kbdataid"] = kbdataid;
        this.proxy.actionMethods = 'post';
        this.proxy.extraParams = { type:'select', param:Ext.JSON.encode(param) };
    }

});