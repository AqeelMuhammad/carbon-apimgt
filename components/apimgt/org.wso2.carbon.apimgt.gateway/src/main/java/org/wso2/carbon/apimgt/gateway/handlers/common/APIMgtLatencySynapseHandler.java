/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;

import java.util.HashMap;
import java.util.Map;

public class APIMgtLatencySynapseHandler extends AbstractSynapseHandler {


    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        TracingTracer tracer = ServiceReferenceHolder.getInstance().getTracer();

        if (Util.tracingEnabled()) {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            Map headersMap =
                    (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            TracingSpan spanContext = Util.extract(tracer, headersMap);
            TracingSpan responseLatencySpan =
                    Util.startSpan(APIMgtGatewayConstants.RESPONSE_LATENCY, spanContext, tracer);
            Util.setTag(responseLatencySpan, APIMgtGatewayConstants.SPAN_KIND, APIMgtGatewayConstants.SERVER);
            GatewayUtils.setRequestRelatedTags(responseLatencySpan, messageContext);
            messageContext.setProperty(APIMgtGatewayConstants.RESPONSE_LATENCY, responseLatencySpan);
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {

        TracingTracer tracer = ServiceReferenceHolder.getInstance().getTracer();
        Map<String, String> tracerSpecificCarrier = new HashMap<>();
        if (Util.tracingEnabled()) {
            TracingSpan parentSpan = (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);
            TracingSpan backendLatencySpan =
                    Util.startSpan(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN, parentSpan, tracer);
            messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN, backendLatencySpan);
            Util.inject(backendLatencySpan, tracer, tracerSpecificCarrier);
            if (org.apache.axis2.context.MessageContext.getCurrentMessageContext() != null) {
                Map headers = (Map) org.apache.axis2.context.MessageContext.getCurrentMessageContext().getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                headers.putAll(tracerSpecificCarrier);
                org.apache.axis2.context.MessageContext.getCurrentMessageContext()
                        .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            }
        }
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        if (Util.tracingEnabled() && messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN) != null) {
            TracingSpan backendLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY_SPAN);
            GatewayUtils.setEndpointRelatedInformation(backendLatencySpan, messageContext);
            Util.finishSpan(backendLatencySpan);
        }
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        if (Util.tracingEnabled()) {
            Object resourceSpanObject = messageContext.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);
            if (resourceSpanObject != null){
                GatewayUtils.setAPIResource((TracingSpan) resourceSpanObject, messageContext);
                Util.finishSpan((TracingSpan) resourceSpanObject);
            }
            TracingSpan responseLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            GatewayUtils.setAPIRelatedTags(responseLatencySpan, messageContext);
            API api = GatewayUtils.getAPI(messageContext);
            if (api!= null){
                Util.updateOperation(responseLatencySpan, api.getApiName().concat("--").concat(api.getApiVersion()).concat("--").concat(GatewayUtils.getTenantDomain()));
            }
            Util.finishSpan(responseLatencySpan);
        }
        return true;
    }
}
