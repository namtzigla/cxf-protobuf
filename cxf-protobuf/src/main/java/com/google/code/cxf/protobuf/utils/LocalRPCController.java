/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.google.code.cxf.protobuf.utils;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * TODO: type comment.
 * 
 */
public class LocalRPCController implements RpcController {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.protobuf.RpcController#errorText()
	 */
	public String errorText() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.protobuf.RpcController#failed()
	 */
	public boolean failed() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.protobuf.RpcController#isCanceled()
	 */
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.protobuf.RpcController#notifyOnCancel(com.google.protobuf.
	 * RpcCallback)
	 */
	public void notifyOnCancel(RpcCallback<Object> arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.protobuf.RpcController#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.protobuf.RpcController#setFailed(java.lang.String)
	 */
	public void setFailed(String arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.protobuf.RpcController#startCancel()
	 */
	public void startCancel() {
		// TODO Auto-generated method stub

	}

}
