/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.mycompany;

import io.milton.common.StreamUtils;
import io.milton.http.OAuth2TokenResponse;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.annotated.CommonResource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.auth.OAuth2Helper;
import io.milton.resource.CollectionResource;
import io.milton.resource.MakeCalendarResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.OAuth2Resource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

public class TFolderResource extends TResource implements PutableResource, MakeCollectionableResource, MakeCalendarResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResource.class);
    ArrayList<Resource> children = new ArrayList<Resource>();

    public TFolderResource(TFolderResource parent, String name) {
        super(parent, name);
        log.debug("created new folder: " + name);
    }

    @Override
    protected Object clone(TFolderResource newParent, String newName) {
        TFolderResource newFolder = new TFolderResource(newParent, newName);
        for (Resource child : parent.getChildren()) {
            TResource res = (TResource) child;
            res.clone(newFolder, child.getName()); // will auto-add to folder
        }
        return newFolder;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() {
        return children;
    }

    static ByteArrayOutputStream readStream(final InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamUtils.readTo(in, bos);
        return bos;
    }

    @Override
    public CollectionResource createCollection(String newName) {
        log.debug("createCollection: " + newName);
        TFolderResource r = new TFolderResource(this, newName);
        return r;
    }

    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
        ByteArrayOutputStream bos = readStream(inputStream);
        log.debug("createNew: " + bos.size() + " - name: " + newName + " current child count: " + this.children.size());
        TResource r = new TBinaryResource(this, newName, bos.toByteArray(), contentType);
        log.debug("new child count: " + this.children.size());
        return r;
    }

    @Override
    public Resource child(String childName) {
        for (Resource r : getChildren()) {
            if (r.getName().equals(childName)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, MalformedURLException {
        PrintWriter pw = new PrintWriter(out);
        pw.print("<html><body>");
        pw.print("<h1>" + this.getName() + "</h1>");
        pw.print("<p>" + this.getClass().getCanonicalName() + "</p>");
        doBody(pw);
        try {
            doOAth2(pw);
        } catch (OAuthSystemException ex) {
            Logger.getLogger(TFolderResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        pw.print("</body>");
        pw.print("</html>");
        pw.flush();
    }

    private void doOAth2(PrintWriter pw) throws OAuthSystemException, MalformedURLException {
        super.setOAuth2ClientId("131804060198305");

        super.setOAuth2Location("https://graph.facebook.com/oauth/authorize");
        super.setOAuth2RedirectURI("http://localhost:8080/");
        super.setOAuth2Step(OAuth2Resource.GRANT_PERMISSION);

        super.setOAuth2ClientSecret("3acb294b071c9aec86d60ae3daf32a93");
        super.setOAuth2TokenLocation("https://graph.facebook.com/oauth/access_token");

        super.setOAuth2UserProfileLocation("https://graph.facebook.com/me");

        OAuth2Helper oAuth2Helper = new OAuth2Helper(null);
        Object obj = oAuth2Helper.checkOAuth2URL(this);
        log.info("-----oAuth2Helper.checkOAuth2URL------" + obj);
        if (obj instanceof URL) {
            String strTemp = ((URL) obj).toString();
            log.info("OAuth2TokenResponse, OAuth2URL={}" + strTemp);
            if (strTemp != null) {
                pw.print("<ul>");
                pw.print("<li><a href='" + strTemp + "'>" + "Authorize(facebook)" + "</a></li>");
                pw.print("</ul>");
            }

        }

    }

    protected void doBody(PrintWriter pw) {
        System.out.println("dobody - " + children.size());
        pw.print("<ul>");
        for (Resource r : this.children) {
            String href = r.getName();
            if (r instanceof CollectionResource) {
                href = href + "/";
            }
            pw.print("<li><a href='" + href + "'>" + r.getName() + "(" + r.getClass().getCanonicalName() + ")" + "</a></li>");
        }
        pw.print("</ul>");
    }

    @Override
    public String getContentType(String accepts) {
        return "text/html";
    }

    public String getCTag() {
        Date d = getMostRecentModDate();
        if (d == null) {
            System.out.println("No ctag");
            return "000";
        } else {

            String s = d.getTime() + "t";
            System.out.println("ctag=" + s);
            return s;
        }
    }

    public Date getMostRecentModDate() {
        Date latest = this.getModifiedDate();
        for (Resource r : this.getChildren()) {
            Date d;
            if (r instanceof TFolderResource) {
                TFolderResource tf = (TFolderResource) r;
                d = tf.getMostRecentModDate();
            } else {
                d = r.getModifiedDate();
            }
            if (d != null && (latest == null || d.after(latest))) {
                latest = d;
            }
        }
        return latest;
    }

    @Override
    public CollectionResource createCalendar(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        TCalendarResource cal = new TCalendarResource(this, newName);
        return cal;
    }
}
