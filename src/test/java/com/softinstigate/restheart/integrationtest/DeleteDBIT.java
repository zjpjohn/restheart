/*
 * RESTHeart - the data REST API server
 * Copyright (C) 2014 SoftInstigate Srl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.softinstigate.restheart.integrationtest;

import com.eclipsesource.json.JsonObject;
import com.softinstigate.restheart.hal.Representation;
import static com.softinstigate.restheart.integrationtest.AbstactIT.adminExecutor;
import com.softinstigate.restheart.utils.HttpStatus;
import io.undertow.util.Headers;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Test;

/**
 *
 * @author Andrea Di Cesare
 */
public class DeleteDBIT extends AbstactIT {

    public DeleteDBIT() {
    }

    @Test
    public void testDeleteDB() throws Exception {
        try {
            Response resp;

            // *** PUT tmpdb
            resp = adminExecutor.execute(Request.Put(dbTmpUri).bodyString("{a:1}", halCT).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));
            check("check put db", resp, HttpStatus.SC_CREATED);

            // try to delete without etag
            resp = adminExecutor.execute(Request.Delete(dbTmpUri));
            check("check delete tmp doc without etag", resp, HttpStatus.SC_CONFLICT);

            // try to delete with wrong etag
            resp = adminExecutor.execute(Request.Delete(dbTmpUri).addHeader(Headers.IF_MATCH_STRING, "pippoetag"));
            check("check delete tmp doc with wrong etag", resp, HttpStatus.SC_PRECONDITION_FAILED);

            resp = adminExecutor.execute(Request.Get(dbTmpUri).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));
            //check("getting etag of tmp doc", resp, HttpStatus.SC_OK);

            JsonObject content = JsonObject.readFrom(resp.returnContent().asString());

            String etag = content.get("_etag").asString();

            // try to delete with correct etag
            resp = adminExecutor.execute(Request.Delete(dbTmpUri).addHeader(Headers.IF_MATCH_STRING, etag));
            check("check delete tmp doc with correct etag", resp, HttpStatus.SC_NO_CONTENT);

            resp = adminExecutor.execute(Request.Get(dbTmpUri).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));
            check("check get deleted tmp doc", resp, HttpStatus.SC_NOT_FOUND);
        }
        finally {
            mongoClient.dropDatabase(dbTmpName);
        }
    }
}
