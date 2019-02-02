package ut.com.csabahorvath.pagecreatelistener;

import org.junit.Test;
import com.csabahorvath.pagecreatelistener.api.MyPluginComponent;
import com.csabahorvath.pagecreatelistener.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}