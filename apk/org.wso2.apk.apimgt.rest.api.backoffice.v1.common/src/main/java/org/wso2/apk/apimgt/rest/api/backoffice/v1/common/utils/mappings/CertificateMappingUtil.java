/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings;

import org.wso2.apk.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.apk.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.CertMetadataDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.CertificateInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.CertificateValidityDTO;

/**
 * This class is responsible for comverting between certificate objects.
 */
public class CertificateMappingUtil {

    /**
     * To convert Instance of {@link CertificateInformationDTO} to {@link CertificateInfoDTO};
     *
     * @param certificateInformationDTO Instance of {@link CertificateInformationDTO}
     * @return converted instance of {@link CertificateInfoDTO}.
     */
    public static CertificateInfoDTO fromCertificateInformationToDTO(
            CertificateInformationDTO certificateInformationDTO) {
        CertificateValidityDTO certificateValidityDTO = new CertificateValidityDTO();
        certificateValidityDTO.setFrom(certificateInformationDTO.getFrom());
        certificateValidityDTO.setTo(certificateInformationDTO.getTo());

        CertificateInfoDTO certificateInfoDTO = new CertificateInfoDTO();
        certificateInfoDTO.setValidity(certificateValidityDTO);
        certificateInfoDTO.setStatus(certificateInformationDTO.getStatus());
        certificateInfoDTO.setSubject(certificateInformationDTO.getSubject());
        certificateInfoDTO.setVersion(certificateInformationDTO.getVersion());

        return certificateInfoDTO;
    }

    public static CertMetadataDTO fromCertificateMetadataToDTO(CertificateMetadataDTO certificateMetadata) {
        CertMetadataDTO certificateDTO = new CertMetadataDTO();
        certificateDTO.setAlias(certificateMetadata.getAlias());
        certificateDTO.setEndpoint(certificateMetadata.getEndpoint());

        return certificateDTO;
    }
}
