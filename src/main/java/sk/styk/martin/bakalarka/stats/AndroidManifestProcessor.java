package sk.styk.martin.bakalarka.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sk.styk.martin.bakalarka.data.AndroidManifestData;
import sk.styk.martin.bakalarka.data.ApkData;
import sk.styk.martin.bakalarka.files.ApkFile;
import sk.styk.martin.bakalarka.stats.helpers.XmlParsingHelper;

import java.io.File;
import java.util.List;

/**
 * Created by Martin Styk on 25.11.2015.
 */
public class AndroidManifestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AndroidManifestProcessor.class);
    private Marker apkNameMarker;
    private Document document;
    private ApkData data;
    private File manifestFile;
    private AndroidManifestData manifestData;
    private ApkFile apkFile;

    public AndroidManifestProcessor(ApkData data, ApkFile apkFile) {
        if (data == null) {
            throw new IllegalArgumentException("data null");
        }
        if (apkFile == null) {
            throw new IllegalArgumentException("apkFile null");
        }

        this.data = data;
        this.apkFile = apkFile;
        this.apkNameMarker = apkFile.getMarker();
    }

    public AndroidManifestProcessor(ApkFile apkFile) {
        if (apkFile == null) {
            throw new IllegalArgumentException("apkFile null");
        }

        this.data = null;
        this.apkFile = apkFile;
        this.apkNameMarker = apkFile.getMarker();
    }

    public static AndroidManifestProcessor getInstance(ApkData data, ApkFile apkFile) {
        return new AndroidManifestProcessor(data, apkFile);
    }

    public static AndroidManifestProcessor getInstance(ApkFile apkFile) {
        return new AndroidManifestProcessor(apkFile);
    }

    public AndroidManifestData processAndroidManifest() {

        manifestData = new AndroidManifestData();

        logger.trace(apkNameMarker + "Started processing AndroidManifest");

        try {

            manifestFile = new File(apkFile.getDecompiledDirectoryWithDecompiledData(), "AndroidManifest.xml");
            document = XmlParsingHelper.getNormalizedDocument(manifestFile);

            getManifestTagData();
            getNumberOfAppComponents();
            getUsedPermissions();
            getUsedLibraries();
            getUsedFeatures();
            getUsesSdk();
            getSupportScreens();

        } catch (Exception e) {
            logger.error(apkNameMarker + e.toString());
        } finally {
            document = null;
        }

        if (data != null) {
            data.setAndroidManifest(manifestData);
        }

        logger.trace(apkNameMarker + "Finished processing of AndroidManifest");

        return manifestData;
    }

    private void getManifestTagData() {
        Element element = XmlParsingHelper.getSingleAppearingElementByTag(document, "manifest");
        if (element != null) {
            manifestData.setPackageName(XmlParsingHelper.getSingleNonEmptyStringAtributeFromElement(element, "package"));
            manifestData.setVersionCode(XmlParsingHelper.getSingleNonEmptyStringAtributeFromElement(element, "android:versionCode"));
            manifestData.setInstallLocation(XmlParsingHelper.getSingleNonEmptyStringAtributeFromElement(element, "android:installLocation"));
        }
    }

    private void getNumberOfAppComponents() {
        NodeList activityList = document.getElementsByTagName("activity");
        NodeList serviceList = document.getElementsByTagName("service");
        NodeList receiverList = document.getElementsByTagName("receiver");
        NodeList providerList = document.getElementsByTagName("provider");

        manifestData.setNumberOfActivities(activityList.getLength());
        manifestData.setNumberOfServices(serviceList.getLength());
        manifestData.setNumberOfBroadcastReceivers(receiverList.getLength());
        manifestData.setNumberOfContentProviders(providerList.getLength());
    }

    private void getUsedPermissions() {
        List<String> result = XmlParsingHelper.getListOfTagAttributeValues(document, "uses-permission", "android:name");
        manifestData.setUsesPermissions(result);
    }

    private void getUsedLibraries() {
        List<String> result = XmlParsingHelper.getListOfTagAttributeValues(document, "uses-library", "android:name");
        manifestData.setUsesLibrary(result);
    }

    private void getUsedFeatures() {
        List<String> result = XmlParsingHelper.getListOfTagAttributeValues(document, "uses-feature", "android:name");
        manifestData.setUsesFeature(result);
    }

    private void getUsesSdk() {
        Element element = XmlParsingHelper.getSingleAppearingElementByTag(document, "uses-sdk");

        if (element != null) {
            manifestData.setUsesTargetSdkVersion(element.getAttribute("android:targetSdkVersion"));
            manifestData.setUsesMinSdkVersion(element.getAttribute("android:minSdkVersion"));
            manifestData.setUsesMaxSdkVersion(element.getAttribute("android:maxSdkVersion"));
        }
    }

    private void getSupportScreens() {
        Element element = XmlParsingHelper.getSingleAppearingElementByTag(document, "supports-screens");

        if (element != null) {
            manifestData.setSupportsScreensResizeable(XmlParsingHelper.getBooleanElementAtribute(element, "android:resizeable"));
            manifestData.setSupportsScreensAnyDensity(XmlParsingHelper.getBooleanElementAtribute(element, "android:anyDensity"));
            manifestData.setSupportsScreensSmall(XmlParsingHelper.getBooleanElementAtribute(element, "android:smallScreens"));
            manifestData.setSupportsScreensNormal(XmlParsingHelper.getBooleanElementAtribute(element, "android:normalScreens"));
            manifestData.setSupportsScreensLarge(XmlParsingHelper.getBooleanElementAtribute(element, "android:largeScreens"));
            manifestData.setSupportsScreensXlarge(XmlParsingHelper.getBooleanElementAtribute(element, "android:xlargeScreens"));
        }
    }

}
