package edu.utah.catalog.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fujion.annotation.Component;
import org.fujion.component.BaseComponent;
import org.fujion.component.BaseUIComponent;
import org.fujion.annotation.Component.ChildTag;

import java.util.Map;

@Component(tag = "catalogcomponent", widgetModule = "catalog-component", widgetClass = "CatalogComponent", parentTag = "*", childTag = @ChildTag("*"))
public class CatalogComponent extends BaseUIComponent {

    private static final Log log = LogFactory.getLog(CatalogComponent.class);

    public CatalogComponent() {
    }

    @Override
    protected void _initProps(Map<String, Object> props) {
        super._initProps(props);
        props.put("wclazz", "catalogcomponent");
    }

}