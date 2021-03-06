/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.wisc.my.portlets.bookmarks.web;

import java.util.Date;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;

import edu.wisc.my.portlets.bookmarks.domain.BookmarkSet;
import edu.wisc.my.portlets.bookmarks.domain.CollectionFolder;
import edu.wisc.my.portlets.bookmarks.domain.Entry;
import edu.wisc.my.portlets.bookmarks.domain.Folder;
import edu.wisc.my.portlets.bookmarks.domain.support.FolderUtils;
import edu.wisc.my.portlets.bookmarks.domain.support.IdPathInfo;

/**
 * <p>NewCollectionFormController class.</p>
 *
 * @author Unknown
 * @version $Id: $Id
 */
public class NewCollectionFormController extends BaseEntryFormController {
    /** {@inheritDoc} */
    @Override
    protected void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {
        final String targetParentPath = StringUtils.defaultIfEmpty(request.getParameter("folderPath"), null);
        
        //User edited bookmark
        final CollectionFolder commandBookmark = (CollectionFolder)command;
        
        //Get the BookmarkSet from the store
        final BookmarkSet bs = this.bookmarkSetRequestResolver.getBookmarkSet(request);
        
        //Ensure the created & modified dates are set correctly
        commandBookmark.setCreated(new Date());
        commandBookmark.setModified(commandBookmark.getCreated());
        
        final Folder targetParent;
        if (targetParentPath != null) {
            //Get the target parent folder
            final IdPathInfo targetParentPathInfo = FolderUtils.getEntryInfo(bs, targetParentPath);
            if (targetParentPathInfo == null || targetParentPathInfo.getTarget() == null) {
                throw new IllegalArgumentException("The specified parent Folder does not exist. BaseFolder='" + bs + "' and idPath='" + targetParentPath + "'");
            }
            
            targetParent = (Folder)targetParentPathInfo.getTarget();
        }
        else {
            targetParent = bs;
        }
        
        final Map<Long, Entry> targetChildren = targetParent.getChildren();
        
        //Add the new bookmark to the target parent
        targetChildren.put(commandBookmark.getId(), commandBookmark);
        
        //Persist the changes to the BookmarkSet 
        this.bookmarkStore.storeBookmarkSet(bs);
    }

}
