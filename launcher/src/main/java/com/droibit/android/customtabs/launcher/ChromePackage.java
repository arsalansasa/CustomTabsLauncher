package com.droibit.android.customtabs.launcher;

import androidx.annotation.RestrictTo;
import java.util.Arrays;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY) interface ChromePackage {

  String PACKAGE_STABLE = "com.android.chrome";
  String PACKAGE_BETA = "com.chrome.beta";
  String PACKAGE_DEV = "com.chrome.dev";
  String PACKAGE_LOCAL = "com.google.android.apps.chrome";

  List<String> CHROME_PACKAGES =
      Arrays.asList(PACKAGE_STABLE, PACKAGE_BETA, PACKAGE_DEV, PACKAGE_LOCAL);
}
