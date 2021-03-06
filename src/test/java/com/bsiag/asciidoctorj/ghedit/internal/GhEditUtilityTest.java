/*******************************************************************************
 * Copyright (c) 2016 Jeremie Bresson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 ******************************************************************************/
package com.bsiag.asciidoctorj.ghedit.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GhEditUtilityTest {

  private static final String DEFAULT_VIEW_LINK_TEXT = "view on GitHub";
  private static final String DEFAULT_EDIT_LINK_TEXT = "edit on GitHub";
  private static final String CUSTOM_LINK_TEXT = "custom link Text";

  private static final String REPO = "jmini/some-repo";
  private static final String FILE = "test/some-repo/my_file.txt";
  private static final String SUB_FILE = "test/some-repo/folder/TEXT.adoc";
  private static final String WRONG_FILE = "test.txt";

  @Test
  public void testComputeFilepath() throws Exception {
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath(FILE, "some-repo"));
    assertEquals("/folder/TEXT.adoc", GhEditUtility.computeFilePath(SUB_FILE, "some-repo"));
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath("some-repo/my_file.txt", "some-repo"));
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath("xxx/some-repo/yyyy/some-repo/my_file.txt", "some-repo"));
    assertEquals("/file.txt", GhEditUtility.computeFilePath("xxx/myproject_repo/file.txt", "myproject"));
    assertEquals("/file.txt", GhEditUtility.computeFilePath("/myproject_repo/file.txt", "myproject"));
    assertEquals("/some/folder/file.txt", GhEditUtility.computeFilePath("xxx/myproject.git/some/folder/file.txt", "myproject"));

    //Handle the special case "target/checkout"
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath("xxx/some-repo/target/checkout/my_file.txt", "some-repo"));
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath("xxx/some-repo/proj/target/checkout/my_file.txt", "some-repo"));
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath("xxx/target/checkout/my_file.txt", "some-repo"));
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath("target/checkout/my_file.txt", "some-repo"));
    assertEquals("/some/folder/file.txt", GhEditUtility.computeFilePath("xxx/some-repo/target/checkout/some/folder/file.txt", "some-repo"));
    assertEquals("/my_file.txt", GhEditUtility.computeFilePath("xxx\\some-repo\\target\\checkout\\my_file.txt", "some-repo"));
  }

  @Test
  public void testNullRepository() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, null, null, null, FILE);
    assertLinkEqual(null, DEFAULT_EDIT_LINK_TEXT, GhEditUtility.WARN_NO_REPOSITORY_SET, link);
  }

  @Test
  public void testNullRepositoryWithText() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, null, null, CUSTOM_LINK_TEXT, FILE);
    assertLinkEqual(null, CUSTOM_LINK_TEXT, GhEditUtility.WARN_NO_REPOSITORY_SET, link);
  }

  @Test
  public void testEmptyRepository() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, "", null, null, FILE);
    assertLinkEqual(null, DEFAULT_EDIT_LINK_TEXT, GhEditUtility.WARN_NO_REPOSITORY_SET, link);
  }

  @Test
  public void testFileUnknownFileAndPath() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, REPO, null, null, null);
    assertLinkEqual(null, DEFAULT_EDIT_LINK_TEXT, GhEditUtility.WARN_FILE_UNKNWON, link);
  }

  @Test
  public void testWrongRepository() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, "xxx", null, null, FILE);
    assertLinkEqual(null, DEFAULT_EDIT_LINK_TEXT, GhEditUtility.WARN_UNEXPECTED_REPOSITORY + "xxx", link);
  }

  @Test
  public void testWrongRepository2() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, "zzz/", null, null, FILE);
    assertLinkEqual(null, DEFAULT_EDIT_LINK_TEXT, GhEditUtility.WARN_UNEXPECTED_REPOSITORY + "zzz/", link);
  }

  @Test
  public void testOkDefaultEdit() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/edit/master/my_file.txt", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkDefaultView() {
    GhEditLink link = GhEditUtility.compute(null, "view", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/blob/master/my_file.txt", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testNullMode() {
    GhEditLink link = GhEditUtility.compute(null, null, null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/blob/master/my_file.txt", DEFAULT_VIEW_LINK_TEXT, GhEditUtility.WARN_UNEXPECTED_MODE, link);
  }

  @Test
  public void testWrongMode() {
    GhEditLink link = GhEditUtility.compute(null, "xxx", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/blob/master/my_file.txt", DEFAULT_VIEW_LINK_TEXT, GhEditUtility.WARN_UNEXPECTED_MODE, link);
  }

  @Test
  public void testOkWithPath1() {
    GhEditLink link = GhEditUtility.compute("path/to/this/File.adoc", "edit", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/edit/master/path/to/this/File.adoc", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkWithPath2() {
    GhEditLink link = GhEditUtility.compute("/path/to/this/File.txt", "edit", null, REPO, null, null, SUB_FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/edit/master/path/to/this/File.txt", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkWithPathUnknownFile() {
    GhEditLink link = GhEditUtility.compute("/path/to/this/File.txt", "edit", null, REPO, null, null, null);
    assertLinkEqual("https://github.com/jmini/some-repo/edit/master/path/to/this/File.txt", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkDefaultForSubFile() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, REPO, null, null, SUB_FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/edit/master/folder/TEXT.adoc", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkCustomBranch() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, REPO, "features/preview", null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/edit/features/preview/my_file.txt", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkCustomText() {
    GhEditLink link = GhEditUtility.compute(null, "edit", null, REPO, null, CUSTOM_LINK_TEXT, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/edit/master/my_file.txt", CUSTOM_LINK_TEXT, null, link);
  }

  @Test
  public void testOkViewDir() {
    GhEditLink link = GhEditUtility.compute("some/folder", "viewdir", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/tree/master/some/folder", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testOkViewDirTrailingSlash() {
    GhEditLink link = GhEditUtility.compute("some/folder/", "viewdir", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/tree/master/some/folder", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testOkViewDirStartWithDot() {
    GhEditLink link = GhEditUtility.compute("./some/folder", "viewdir", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/tree/master/some/folder", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testOkViewDirDotSlash() {
    GhEditLink link = GhEditUtility.compute("./", "viewdir", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/tree/master", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testOkViewDirDotSlashOtherBranch() {
    GhEditLink link = GhEditUtility.compute("./", "viewdir", null, REPO, "gh-pages", null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/tree/gh-pages", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testEditButShouldBeViewDir() {
    GhEditLink link = GhEditUtility.compute("some/folder/", "edit", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/tree/master/some/folder", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testViewButShouldBeViewDir() {
    GhEditLink link = GhEditUtility.compute("some/folder/", "view", null, REPO, null, null, FILE);
    assertLinkEqual("https://github.com/jmini/some-repo/tree/master/some/folder", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  @Test
  public void testOkOtherServer() {
    GhEditLink link = GhEditUtility.compute(null, "edit", "https://git.server.com", REPO, null, null, FILE);
    assertLinkEqual("https://git.server.com/jmini/some-repo/edit/master/my_file.txt", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkOtherServerTrailingSlash() {
    GhEditLink link = GhEditUtility.compute(null, "edit", "https://git.server.com/", REPO, null, null, FILE);
    assertLinkEqual("https://git.server.com/jmini/some-repo/edit/master/my_file.txt", DEFAULT_EDIT_LINK_TEXT, null, link);
  }

  @Test
  public void testOkOtherServerOtherBranch() {
    GhEditLink link = GhEditUtility.compute(null, "view", "https://server.com/git-manager", REPO, "features/test", null, FILE);
    assertLinkEqual("https://server.com/git-manager/jmini/some-repo/blob/features/test/my_file.txt", DEFAULT_VIEW_LINK_TEXT, null, link);
  }

  private static void assertLinkEqual(String url, String text, String warning, GhEditLink actual) {
    assertEquals("url", url, actual.getUrl());
    assertEquals("text", text, actual.getText());
    assertEquals("warning", warning, actual.getWarning());
  }

}
