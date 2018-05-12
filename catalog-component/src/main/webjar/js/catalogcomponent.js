'use strict';

define('catalog-component', [
	'fujion-core', 
	'fujion-widget',
	'catalog-component-css'
	], 
	
	function(fujion, Widget) { 
	
	var CatalogComponent = Widget.UIWidget.extend({
	
		/*------------------------------ Containment ------------------------------*/

		
		/*------------------------------ Lifecycle ------------------------------*/

		init: function() {
			this._super();
		},
		
		/*------------------------------ Other ------------------------------*/
	
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<label></label>');
		}
		
		/*------------------------------ State ------------------------------*/
		
	});

	return Widget.addon('edu.utah', 'CatalogComponent', CatalogComponent);
});