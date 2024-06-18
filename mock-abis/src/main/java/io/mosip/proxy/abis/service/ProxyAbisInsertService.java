package io.mosip.proxy.abis.service;

import org.springframework.web.multipart.MultipartFile;

import io.mosip.proxy.abis.dto.IdentifyDelayResponse;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.InsertRequestMO;

/**
 * Service interface defining operations for interacting with an Automated
 * Biometric Identification System (ABIS). This includes functionalities for
 * data insertion, deletion, and duplication search based on biometric data.
 */
public interface ProxyAbisInsertService {
	/**
	 * Deletes biometric data associated with the provided reference ID from the
	 * ABIS.
	 *
	 * @param referenceId The reference ID of the biometric data to be deleted.
	 */
	public void deleteData(String referenceId);

	/**
	 * Inserts new biometric data represented by the InsertRequestMO object into the
	 * ABIS.
	 *
	 * @param insertRequest The InsertRequestMO object containing the data to be
	 *                      inserted.
	 * @return The number of records inserted (implementation specific).
	 */
	public int insertData(InsertRequestMO ie);

	/**
	 * Searches for potential duplicate biometric data based on the information
	 * provided in the IdentityRequest object.
	 *
	 * @param identityRequest The IdentityRequest object containing search criteria.
	 * @return An IdentifyDelayResponse object containing details about potential
	 *         matches and any associated delays.
	 */
	public IdentifyDelayResponse findDuplication(IdentityRequest ir);

	/**
	 * Saves the uploaded file along with the provided parameters.
	 *
	 * @param uploadedFile The uploaded file.
	 * @param alias        The alias for the file.
	 * @param password     The password for the file.
	 * @param keystore     The keystore.
	 * @return A string representing the result of the file saving process.
	 */
	public String saveUploadedFileWithParameters(MultipartFile upoadedFile, String alias, String password,
			String keystore);
}