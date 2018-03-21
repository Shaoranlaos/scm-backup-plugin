registerGeneralConfigPanel({
  xtype : 'configForm',
  title : 'Backup to SVN',
  items : [
	  {
		xtype: 'checkbox',
		fieldLabel: 'Enabled',
		name : 'active',
		inputValue : 'true'
	  },
	  {
		xtype : 'textfield',
		fieldLabel : 'Remote SVN Server URL',
		name : 'remoteSvnServer',
		allowBlank : false,
		vtype: 'url'
	  },
	  {
		xtype : 'textfield',
		fieldLabel : 'Remote SVN User',
		name : 'remoteSvnUser',
		allowBlank : false
	  },
	  {
		xtype : 'textfield',
		inputType: 'password',
		fieldLabel : 'Remote SVN Password',
		name : 'remoteSvnPassword',
		allowBlank : false
	  },
	  {
		  xtype : 'textfield',
    	fieldLabel : 'Backup Rate (in min)',
    	name : 'backupRate',
    	allowBlank : false
	  },
	  {
		xtype : 'textfield',
		fieldLabel : 'local backup path',
		name : 'localBackupPath',
		allowBlank : false
	  }
	],

  onSubmit: function(values){
    this.el.mask('Submit ...');
    Ext.Ajax.request({
      url: restUrl + 'config/backup/to-svn.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
      }
    });
  },

  onLoad: function(el){
    var tid = setTimeout( function(){ el.mask('Loading ...'); }, 100);
    Ext.Ajax.request({
      url: restUrl + 'config/backup/to-svn.json',
      method: 'GET',
      scope: this,
      disableCaching: true,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        clearTimeout(tid);
        el.unmask();
      },
      failure: function(){
        el.unmask();
        clearTimeout(tid);
        alert('failure');
      }
    });
  }
});