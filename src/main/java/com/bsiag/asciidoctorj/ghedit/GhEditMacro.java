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
package com.bsiag.asciidoctorj.ghedit;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Inline;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.jruby.RubySymbol;

import com.bsiag.asciidoctorj.ghedit.internal.GhEditLink;
import com.bsiag.asciidoctorj.ghedit.internal.GhEditUtility;

public class GhEditMacro extends InlineMacroProcessor {

  public GhEditMacro(String macroName, Map<String, Object> config) {
    super(macroName, config);
  }

  @Override
  protected Object process(AbstractBlock parent, String path, Map<String, Object> attributes) {
    Object docFile = searchDocFile(parent);

    Object linkText = searchAttribute(attributes, "link-text", 1, parent, null);
    Object mode = searchAttribute(attributes, "mode", 2, parent, "git-link-mode");
    Object branch = searchAttribute(attributes, "branch", 3, parent, "git-branch");
    Object repository = searchAttribute(attributes, "repository", 4, parent, "git-repository");
    Object linkWindow = searchAttribute(attributes, "link-window", 5, parent, null);
    Object server = searchAttribute(attributes, "server", 6, parent, "git-server");

    GhEditLink link = GhEditUtility.compute(path, mode, server, repository, branch, linkText, docFile);

    if (link.getWarning() != null) {
      //TODO: log a warning containing link.getWarning()
    }

    if (link.getUrl() == null) {
      return link.getText();
    }
    else {
      // Define options for an 'anchor' element:
      Map<String, Object> options = new HashMap<String, Object>();
      options.put("type", ":link");
      options.put("target", link.getUrl());

      // Define attribute for an 'anchor' element:
      if (linkWindow != null && !linkWindow.toString().isEmpty()) {
        attributes.put("window", linkWindow.toString());
      }

      // Create the 'anchor' node:
      Inline inline = createInline(parent, "anchor", link.getText(), attributes, options);

      // Convert to String value:
      return inline.convert();
    }
  }

  private Object searchAttribute(Map<String, Object> attributes, String attrKey, int attrPosition, AbstractBlock parent, String attrDocumentKey) {
    Object result;
    //Try to get the attribute by key:
    result = attributes.get(attrKey);
    if (result != null) {
      return result;
    }
    //Try to get the attribute by position:
    result = attributes.get(attrPosition);
    if (result != null) {
      return result;
    }
    //Try to get the attribute in the document:
    if (attrDocumentKey != null) {
      return parent.getDocument().getAttr(attrDocumentKey);
    }
    //Not found:
    return null;
  }

  /**
   * @param parent
   * @return
   */
  private String searchDocFile(AbstractBlock parent) {
    Map<Object, Object> options = parent.getDocument().getOptions();
    for (Object optionKeyObj : options.keySet()) {
      if (optionKeyObj instanceof RubySymbol) {
        if ("attributes".equals(((RubySymbol) optionKeyObj).toJava(String.class))) {
          Object attributesObj = options.get(optionKeyObj);
          if (attributesObj instanceof Map<?, ?>) {
            Map<?, ?> attributes = (Map<?, ?>) attributesObj;
            Object docfile = attributes.get("docfile");
            if (docfile != null) {
              return docfile.toString();
            }
          }
        }
      }
    }
    return null;
  }
}
