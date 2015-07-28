package freemarker.core;

import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.SimpleObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

@SuppressWarnings("boxing")
public class TemplateConfigurerTest {

    private static final Version ICI = Configuration.VERSION_2_3_22;

    private static final Configuration DEFAULT_CFG = new Configuration(ICI);

    private static final TimeZone NON_DEFAULT_TZ;

    static {
        TimeZone defaultTZ = DEFAULT_CFG.getTimeZone();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        if (tz.equals(defaultTZ)) {
            tz = TimeZone.getTimeZone("GMT+01");
            if (tz.equals(defaultTZ)) {
                throw new AssertionError("Couldn't chose a non-default time zone");
            }
        }
        NON_DEFAULT_TZ = tz;
    }

    private static final Locale NON_DEFAULT_LOCALE;

    static {
        Locale defaultLocale = DEFAULT_CFG.getLocale();
        Locale locale = Locale.GERMAN;
        if (locale.equals(defaultLocale)) {
            locale = Locale.US;
            if (locale.equals(defaultLocale)) {
                throw new AssertionError("Couldn't chose a non-default locale");
            }
        }
        NON_DEFAULT_LOCALE = locale;
    }

    private static final String NON_DEFAULT_ENCODING;

    static {
        String defaultEncoding = DEFAULT_CFG.getDefaultEncoding();
        String encoding = "UTF-16";
        if (encoding.equals(defaultEncoding)) {
            encoding = "UTF-8";
            if (encoding.equals(defaultEncoding)) {
                throw new AssertionError("Couldn't chose a non-default locale");
            }
        }
        NON_DEFAULT_ENCODING = encoding;
    }

    private static final Map<String, Object> SETTING_ASSIGNMENTS;

    static {
        SETTING_ASSIGNMENTS = new HashMap<String, Object>();

        // Non-parser settings:
        SETTING_ASSIGNMENTS.put("APIBuiltinEnabled", true);
        SETTING_ASSIGNMENTS.put("SQLDateAndTimeTimeZone", NON_DEFAULT_TZ);
        SETTING_ASSIGNMENTS.put("URLEscapingCharset", "utf-16");
        SETTING_ASSIGNMENTS.put("arithmeticEngine", ArithmeticEngine.CONSERVATIVE_ENGINE);
        SETTING_ASSIGNMENTS.put("autoFlush", false);
        SETTING_ASSIGNMENTS.put("booleanFormat", "J,N");
        SETTING_ASSIGNMENTS.put("classicCompatibleAsInt", 2);
        SETTING_ASSIGNMENTS.put("dateFormat", "yyyy-#DDD");
        SETTING_ASSIGNMENTS.put("dateTimeFormat", "yyyy-#DDD-@HH:mm");
        SETTING_ASSIGNMENTS.put("locale", NON_DEFAULT_LOCALE);
        SETTING_ASSIGNMENTS.put("logTemplateExceptions", false);
        SETTING_ASSIGNMENTS.put("newBuiltinClassResolver", TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
        SETTING_ASSIGNMENTS.put("numberFormat", "0.0000");
        SETTING_ASSIGNMENTS.put("objectWrapper", new SimpleObjectWrapper(ICI));
        SETTING_ASSIGNMENTS.put("outputEncoding", "utf-16");
        SETTING_ASSIGNMENTS.put("showErrorTips", false);
        SETTING_ASSIGNMENTS.put("templateExceptionHandler", TemplateExceptionHandler.IGNORE_HANDLER);
        SETTING_ASSIGNMENTS.put("timeFormat", "@HH:mm");
        SETTING_ASSIGNMENTS.put("timeZone", NON_DEFAULT_TZ);

        // Parser settings:
        SETTING_ASSIGNMENTS.put("tagSyntax", Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        SETTING_ASSIGNMENTS.put("namingConvention", Configuration.LEGACY_NAMING_CONVENTION);
        SETTING_ASSIGNMENTS.put("whitespaceStripping", false);
        SETTING_ASSIGNMENTS.put("strictSyntaxMode", false);
        SETTING_ASSIGNMENTS.put("encoding", NON_DEFAULT_ENCODING);
    }
    
    public static String getIsSetMethodName(String readMethodName) {
        String isSetMethodName = (readMethodName.startsWith("get") ? "is" + readMethodName.substring(3)
                : readMethodName)
                + "Set";
        if (isSetMethodName.equals("isClassicCompatibleAsIntSet")) {
            isSetMethodName = "isClassicCompatibleSet";
        }
        return isSetMethodName;
    }

    public static List<PropertyDescriptor> getTemplateConfigurerSettingPropDescs(boolean includeCompilerSettings)
            throws IntrospectionException {
        List<PropertyDescriptor> settingPropDescs = new ArrayList<PropertyDescriptor>();

        BeanInfo beanInfo = Introspector.getBeanInfo(TemplateConfigurer.class);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            String name = pd.getName();
            if (pd.getWriteMethod() != null && !IGNORED_PROP_NAMES.contains(name)
                    && !(!includeCompilerSettings && COMPILER_PROP_NAMES.contains(name))) {
                if (pd.getReadMethod() == null) {
                    throw new AssertionError("Property has no read method: " + pd);
                }
                settingPropDescs.add(pd);
            }
        }

        Collections.sort(settingPropDescs, new Comparator<PropertyDescriptor>() {

            public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return settingPropDescs;
    }

    private static final Set<String> IGNORED_PROP_NAMES;

    static {
        IGNORED_PROP_NAMES = new HashSet();
        IGNORED_PROP_NAMES.add("class");
        IGNORED_PROP_NAMES.add("strictBeanModels");
        IGNORED_PROP_NAMES.add("parentConfiguration");
        IGNORED_PROP_NAMES.add("settings");
        IGNORED_PROP_NAMES.add("classicCompatible");
    }

    private static final Set<String> COMPILER_PROP_NAMES;

    static {
        COMPILER_PROP_NAMES = new HashSet();
        for (Method m : ParserConfiguration.class.getMethods()) {
            String propertyName;
            if (m.getName().startsWith("get")) {
                propertyName = m.getName().substring(3);
            } else if (m.getName().startsWith("is")) {
                propertyName = m.getName().substring(2);
            } else {
                propertyName = null;
            }
            if (propertyName != null) {
                if (!Character.isUpperCase(propertyName.charAt(1))) {
                    propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                }
                COMPILER_PROP_NAMES.add(propertyName);
            }
        }
        // Add extra compiler settings here.
        COMPILER_PROP_NAMES.add("encoding");
    }
    
    private static final CustomAttribute CA1 = new CustomAttribute(CustomAttribute.SCOPE_TEMPLATE); 
    private static final CustomAttribute CA2 = new CustomAttribute(CustomAttribute.SCOPE_TEMPLATE); 
    private static final CustomAttribute CA3 = new CustomAttribute(CustomAttribute.SCOPE_TEMPLATE); 
    private static final CustomAttribute CA4 = new CustomAttribute(CustomAttribute.SCOPE_TEMPLATE); 

    @Test
    public void testMergeBasicFunctionality() throws Exception {
        for (PropertyDescriptor propDesc1 : getTemplateConfigurerSettingPropDescs(true)) {
            for (PropertyDescriptor propDesc2 : getTemplateConfigurerSettingPropDescs(true)) {
                TemplateConfigurer tc1 = new TemplateConfigurer();
                TemplateConfigurer tc2 = new TemplateConfigurer();

                Object value1 = SETTING_ASSIGNMENTS.get(propDesc1.getName());
                propDesc1.getWriteMethod().invoke(tc1, value1);
                Object value2 = SETTING_ASSIGNMENTS.get(propDesc2.getName());
                propDesc2.getWriteMethod().invoke(tc2, value2);

                TemplateConfigurer tcm = TemplateConfigurer.merge(tc1, tc2);
                Object mValue1 = propDesc1.getReadMethod().invoke(tcm);
                Object mValue2 = propDesc2.getReadMethod().invoke(tcm);

                assertEquals("For " + propDesc1.getName(), value1, mValue1);
                assertEquals("For " + propDesc2.getName(), value2, mValue2);
            }
        }
    }
    
    @Test
    public void testMergePriority() throws Exception {
        TemplateConfigurer tc1 = new TemplateConfigurer();
        tc1.setDateFormat("1");
        tc1.setTimeFormat("1");
        tc1.setDateTimeFormat("1");

        TemplateConfigurer tc2 = new TemplateConfigurer();
        tc2.setDateFormat("2");
        tc2.setTimeFormat("2");

        TemplateConfigurer tc3 = new TemplateConfigurer();
        tc3.setDateFormat("3");

        TemplateConfigurer tcm = TemplateConfigurer.merge(tc1, tc2, tc3);

        assertEquals("3", tcm.getDateFormat());
        assertEquals("2", tcm.getTimeFormat());
        assertEquals("1", tcm.getDateTimeFormat());
    }
    
    @Test
    public void testMergeCustomAttributes() throws Exception {
        TemplateConfigurer tc1 = new TemplateConfigurer();
        tc1.setCustomAttribute("k1", "v1");
        tc1.setCustomAttribute("k2", "v1");
        tc1.setCustomAttribute("k3", "v1");
        CA1.set("V1", tc1);
        CA2.set("V1", tc1);
        CA3.set("V1", tc1);

        TemplateConfigurer tc2 = new TemplateConfigurer();
        tc2.setCustomAttribute("k1", "v2");
        tc2.setCustomAttribute("k2", "v2");
        CA1.set("V2", tc2);
        CA2.set("V2", tc2);

        TemplateConfigurer tc3 = new TemplateConfigurer();
        tc3.setCustomAttribute("k1", "v3");
        CA1.set("V3", tc2);

        TemplateConfigurer tcm = TemplateConfigurer.merge(tc1, tc2, tc3);

        assertEquals("v3", tcm.getCustomAttribute("k1"));
        assertEquals("v2", tcm.getCustomAttribute("k2"));
        assertEquals("v1", tcm.getCustomAttribute("k3"));
        assertEquals("V3", CA1.get(tcm));
        assertEquals("V2", CA2.get(tcm));
        assertEquals("V1", CA3.get(tcm));
    }
    
    @Test
    public void testMergeNullCustomAttributes() throws Exception {
        TemplateConfigurer tc1 = new TemplateConfigurer();
        tc1.setCustomAttribute("k1", "v1");
        tc1.setCustomAttribute("k2", "v1");
        tc1.setCustomAttribute(null, "v1");
        CA1.set("V1", tc1);
        CA2.set("V1", tc1);
        CA3.set(null, tc1);
        
        assertEquals("v1", tc1.getCustomAttribute("k1"));
        assertEquals("v1", tc1.getCustomAttribute("k2"));
        assertNull("v1", tc1.getCustomAttribute("k3"));
        assertEquals("V1", CA1.get(tc1));
        assertEquals("V1", CA2.get(tc1));
        assertNull(CA3.get(tc1));

        TemplateConfigurer tc2 = new TemplateConfigurer();
        tc2.setCustomAttribute("k1", "v2");
        tc2.setCustomAttribute("k2", null);
        CA1.set("V2", tc2);
        CA2.set(null, tc2);

        TemplateConfigurer tc3 = new TemplateConfigurer();
        tc3.setCustomAttribute("k1", null);
        CA1.set(null, tc2);

        TemplateConfigurer tcm = TemplateConfigurer.merge(tc1, tc2, tc3);

        assertNull(tcm.getCustomAttribute("k1"));
        assertNull(tcm.getCustomAttribute("k2"));
        assertNull(tcm.getCustomAttribute("k3"));
        assertNull(CA1.get(tcm));
        assertNull(CA2.get(tcm));
        assertNull(CA3.get(tcm));
        
        TemplateConfigurer tc4 = new TemplateConfigurer();
        tc4.setCustomAttribute("k1", "v4");
        CA1.set("V4", tc4);
        
        tcm = TemplateConfigurer.merge(tcm, tc4);
        
        assertEquals("v4", tcm.getCustomAttribute("k1"));
        assertNull(tcm.getCustomAttribute("k2"));
        assertNull(tcm.getCustomAttribute("k3"));
        assertEquals("V4", CA1.get(tcm));
        assertNull(CA2.get(tcm));
        assertNull(CA3.get(tcm));
    }
    

    @Test
    public void testConfigureNonParserConfig() throws Exception {
        for (PropertyDescriptor pd : getTemplateConfigurerSettingPropDescs(false)) {
            TemplateConfigurer tc = new TemplateConfigurer();
            tc.setParentConfiguration(DEFAULT_CFG);
    
            Object newValue = SETTING_ASSIGNMENTS.get(pd.getName());
            pd.getWriteMethod().invoke(tc, newValue);
            
            Template t = new Template(null, "", DEFAULT_CFG);
            Method tReaderMethod = t.getClass().getMethod(pd.getReadMethod().getName());
            
            assertNotEquals("For \"" + pd.getName() + "\"", newValue, tReaderMethod.invoke(t));
            tc.configure(t);
            assertEquals("For \"" + pd.getName() + "\"", newValue, tReaderMethod.invoke(t));
        }
    }
    
    @Test
    public void testConfigureCustomAttributes() throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setCustomAttribute("k5", "c");
        cfg.setCustomAttribute("k6", "c");
        
        TemplateConfigurer tc = new TemplateConfigurer();
        tc.setCustomAttribute("k1", "v");
        tc.setCustomAttribute("k2", "v");
        tc.setCustomAttribute("k3", null);
        tc.setCustomAttribute("k6", null);
        CA1.set("V", tc);
        CA2.set("V", tc);
        CA3.set(null, tc);
        
        tc.setParentConfiguration(cfg);
        
        Template t = new Template(null, "", cfg);
        t.setCustomAttribute("k2", "t");
        t.setCustomAttribute("k3", "t");
        t.setCustomAttribute("k4", "t");
        CA2.set("T", t);
        CA4.set("T", t);
        
        tc.configure(t);
        
        assertEquals("v", t.getCustomAttribute("k1"));
        assertEquals("v", t.getCustomAttribute("k2"));
        assertNull(t.getCustomAttribute("k3"));
        assertEquals("t", t.getCustomAttribute("k4"));
        assertEquals("c", t.getCustomAttribute("k5"));
        assertNull(t.getCustomAttribute("k6"));
        assertEquals("V", CA1.get(t));
        assertEquals("V", CA2.get(t));
        assertNull(CA3.get(t));
        assertEquals("T", CA4.get(t));
    }
    

    @Test
    public void testIsSet() throws Exception {
        for (PropertyDescriptor pd : getTemplateConfigurerSettingPropDescs(true)) {
            TemplateConfigurer tc = new TemplateConfigurer();
            checkAllIsSetFalseExcept(tc, null);
            pd.getWriteMethod().invoke(tc, SETTING_ASSIGNMENTS.get(pd.getName()));
            checkAllIsSetFalseExcept(tc, pd.getName());
        }
    }

    private void checkAllIsSetFalseExcept(TemplateConfigurer tc, String trueSetting)
            throws SecurityException, IntrospectionException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for (PropertyDescriptor pd : getTemplateConfigurerSettingPropDescs(true)) {
            String isSetMethodName = getIsSetMethodName(pd.getReadMethod().getName());
            Method isSetMethod;
            try {
                isSetMethod = TemplateConfigurer.class.getMethod(isSetMethodName);
            } catch (NoSuchMethodException e) {
                fail("Missing " + isSetMethodName + " method for \"" + pd.getName() + "\".");
                return;
            }
            if (pd.getName().equals(trueSetting)) {
                assertTrue((Boolean) (isSetMethod.invoke(tc)));
            } else {
                assertFalse((Boolean) (isSetMethod.invoke(tc)));
            }
        }
    }

    /**
     * Test case self-check.
     */
    @Test
    public void checkTestAssignments() throws Exception {
        for (PropertyDescriptor pd : getTemplateConfigurerSettingPropDescs(true)) {
            String propName = pd.getName();
            if (!SETTING_ASSIGNMENTS.containsKey(propName)) {
                fail("Test case doesn't cover all settings in SETTING_ASSIGNMENTS. Missing: " + propName);
            }
            Method readMethod = pd.getReadMethod();
            String cfgMethodName = readMethod.getName();
            if (cfgMethodName.equals("getEncoding")) {
                // Because Configuration has local-to-encoding map too, this has a different name there.
                cfgMethodName = "getDefaultEncoding";
            }
            Method cfgMethod = DEFAULT_CFG.getClass().getMethod(cfgMethodName, readMethod.getParameterTypes());
            Object defaultSettingValue = cfgMethod.invoke(DEFAULT_CFG);
            Object assignedValue = SETTING_ASSIGNMENTS.get(propName);
            assertNotEquals("SETTING_ASSIGNMENTS must contain a non-default value for " + propName,
                    assignedValue, defaultSettingValue);

            TemplateConfigurer tc = new TemplateConfigurer();
            try {
                pd.getWriteMethod().invoke(tc, assignedValue);
            } catch (Exception e) {
                throw new IllegalStateException("For setting \"" + propName + "\" and assigned value of type "
                        + (assignedValue != null ? assignedValue.getClass().getName() : "Null"),
                        e);
            }
        }
    }
    
    
}