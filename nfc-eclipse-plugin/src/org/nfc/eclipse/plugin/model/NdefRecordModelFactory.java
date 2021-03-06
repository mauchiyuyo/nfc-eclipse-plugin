/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package org.nfc.eclipse.plugin.model;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.nfc.eclipse.plugin.util.FileDialogUtil;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.GenericExternalTypeRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.unknown.unsupported.UnsupportedRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.CollisionResolutionRecord;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord.CarrierTypeFormat;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;
import org.nfctools.ndef.wkt.records.WellKnownRecord;


public class NdefRecordModelFactory {
	
	private static final String NO_BYTES = "Zero bytes";
	private static final String N_BYTES = "%d bytes";
	
	public static final String getNoBytesString() {
		return NO_BYTES;
	}

	public static final String getBytesString(int count) {
		return String.format(N_BYTES, count);
	}

	public static NdefRecordModelParent represent(Record[] records) {
				
		NdefRecordModelParent ndefRecordModelParent = new NdefRecordModelParent(null);

		for(Record record : records) {
			ndefRecordModelParent.add(getNode(record, ndefRecordModelParent));
		}
		
		return ndefRecordModelParent;
		
	}
	public static NdefRecordModelNode getNode(String name, String value, NdefRecordModelParent ndefRecordModelParent) {
		return new NdefRecordModelProperty(name, value, ndefRecordModelParent);
	}
	
	public static NdefRecordModelRecord getNode(Record record, NdefRecordModelParent ndefRecordModelParent) {
		if(record instanceof AndroidApplicationRecord) {
			AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			NdefRecordModelProperty ndefRecordModelProperty;
			if(androidApplicationRecord.hasPackageName()) {
				ndefRecordModelProperty = new NdefRecordModelProperty("Package name", androidApplicationRecord.getPackageName(), ndefRecordModelRecord);
			} else {
				ndefRecordModelProperty = new NdefRecordModelProperty("Package name", "", ndefRecordModelRecord);
			}
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof GenericExternalTypeRecord) {
			GenericExternalTypeRecord externalTypeRecord = (GenericExternalTypeRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			if(externalTypeRecord.hasDomain()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Domain", externalTypeRecord.getDomain(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Domain", "", ndefRecordModelRecord));
			}
			
			if(externalTypeRecord.hasType()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Type", externalTypeRecord.getType(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Type", "", ndefRecordModelRecord));
			}

			if(externalTypeRecord.hasData()) {
				ndefRecordModelRecord.add(new NdefRecordModelBinaryProperty("Content", String.format(N_BYTES, externalTypeRecord.getData().length), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelBinaryProperty("Content", NO_BYTES, ndefRecordModelRecord));
			}
			return ndefRecordModelRecord;
		} else if(record instanceof AbsoluteUriRecord) {
			AbsoluteUriRecord uriRecord = (AbsoluteUriRecord)record;
				
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			NdefRecordModelProperty ndefRecordModelProperty;
			if(uriRecord.hasUri()) {
				ndefRecordModelProperty = new NdefRecordModelProperty("URI", uriRecord.getUri(), ndefRecordModelRecord);
			} else {
				ndefRecordModelProperty = new NdefRecordModelProperty("URI", "", ndefRecordModelRecord);
			}
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof SmartPosterRecord) {
			SmartPosterRecord smartPosterRecord = (SmartPosterRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			if(smartPosterRecord.hasTitle()) {
				ndefRecordModelRecord.add(getNode(smartPosterRecord.getTitle(), ndefRecordModelRecord));
			}
			if(smartPosterRecord.hasUri()) {
				ndefRecordModelRecord.add(getNode(smartPosterRecord.getUri(), ndefRecordModelRecord));
			}
			if(smartPosterRecord.hasAction()) {
				ndefRecordModelRecord.add(getNode(smartPosterRecord.getAction(), ndefRecordModelRecord));
			}
			return ndefRecordModelRecord;
		} else if(record instanceof TextRecord) {
			TextRecord textRecord = (TextRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			if(textRecord.hasText()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Text", textRecord.getText(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Text", "", ndefRecordModelRecord));
			}
			
			if(textRecord.hasLocale()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Locale", NdefTextRecordLocale.getLocaleString(textRecord.getLocale()), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Locale", "", ndefRecordModelRecord));
			}
			
			if(textRecord.hasEncoding()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Encoding", textRecord.getEncoding().displayName(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Encoding", "", ndefRecordModelRecord));
			}
			return ndefRecordModelRecord;
		} else if(record instanceof ActionRecord) {
			ActionRecord actionRecord = (ActionRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			NdefRecordModelProperty ndefRecordModelProperty;
			if(actionRecord.hasAction()) {
				ndefRecordModelProperty = new NdefRecordModelProperty("Action", actionRecord.getAction().toString(), ndefRecordModelRecord);
			} else {
				ndefRecordModelProperty = new NdefRecordModelProperty("Action", "", ndefRecordModelRecord);
			}
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof MimeRecord) {
			MimeRecord mimeMediaRecord = (MimeRecord)record;

			// go with binary from here
			BinaryMimeRecord binaryMimeRecord;
			if(mimeMediaRecord instanceof BinaryMimeRecord) {
				binaryMimeRecord = (BinaryMimeRecord)mimeMediaRecord;
			} else {
				binaryMimeRecord = new BinaryMimeRecord(mimeMediaRecord.getContentType(), mimeMediaRecord.getContentAsBytes());
			}
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(binaryMimeRecord, ndefRecordModelParent);

			if(binaryMimeRecord.hasContentType()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Mime-type", binaryMimeRecord.getContentType(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Mime-type", "", ndefRecordModelRecord));
			}
			
			byte[] payload;
			if(binaryMimeRecord.hasContent()) {
				payload = binaryMimeRecord.getContentAsBytes();
			} else {
				payload = null;
			}

			if(payload != null && payload.length > 0) {
				ndefRecordModelRecord.add(new NdefRecordModelBinaryProperty("Content", String.format(N_BYTES, payload.length), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelBinaryProperty("Content", NO_BYTES, ndefRecordModelRecord));
			}

			// remember content type for file dialogs
			if(binaryMimeRecord.hasContentType()) {
				FileDialogUtil.registerMimeType(binaryMimeRecord.getContentType());
			}
			
			return ndefRecordModelRecord;
		} else if(record instanceof UnknownRecord) {
			UnknownRecord unknownRecord = (UnknownRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);
			
			if(unknownRecord.hasPayload()) {
				ndefRecordModelRecord.add(new NdefRecordModelBinaryProperty("Payload", String.format(N_BYTES, unknownRecord.getPayload().length), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelBinaryProperty("Payload", NO_BYTES, ndefRecordModelRecord));
			}
			
			return ndefRecordModelRecord;
		} else if(record instanceof HandoverCarrierRecord) {
			HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			CarrierTypeFormat carrierTypeFormat = handoverCarrierRecord.getCarrierTypeFormat();
			if(carrierTypeFormat != null) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier type format", carrierTypeFormat.toString(), ndefRecordModelRecord));
			
				Object carrierType = handoverCarrierRecord.getCarrierType();
				
				NdefRecordModelParentProperty ndefRecordModelParentProperty = new NdefRecordModelParentProperty("Carrier type", ndefRecordModelRecord);
				
				ndefRecordModelRecord.add(ndefRecordModelParentProperty);

				if(carrierType != null) {
					
					switch(carrierTypeFormat) {
						case WellKnown : {
							// NFC Forum well-known type [NFC RTD]
							if(carrierType instanceof WellKnownRecord) {
								WellKnownRecord abstractWellKnownRecord = (WellKnownRecord)carrierType;
								
								ndefRecordModelParentProperty.add(getNode(abstractWellKnownRecord, ndefRecordModelParentProperty));
			
								break;
							} else {
								throw new IllegalArgumentException();
							}
						}
						case Media : {
							// Media-type as defined in RFC 2046 [RFC 2046]
							String string = (String)carrierType;
							
							ndefRecordModelParentProperty.add(new NdefRecordModelProperty("Media type", string, ndefRecordModelParentProperty));
							break;
						}
						case AbsoluteURI : {
							// Absolute URI as defined in RFC 3986 [RFC 3986]
							String string = (String)carrierType;
							
							ndefRecordModelParentProperty.add(new NdefRecordModelProperty("Absolute URI", string, ndefRecordModelParentProperty));
							break;
						}
						case External : {
							// NFC Forum external type [NFC RTD]
							if(carrierType instanceof GenericExternalTypeRecord) {
								GenericExternalTypeRecord externalTypeRecord = (GenericExternalTypeRecord)carrierType;
								
								ndefRecordModelParentProperty.add(getNode(externalTypeRecord, ndefRecordModelParentProperty));
												
								break;
							} else {
								throw new IllegalArgumentException();
							}
						}
						default: {
							throw new RuntimeException();
						}
					}
				}
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier type format", "", ndefRecordModelRecord));
				
				ndefRecordModelRecord.add(new NdefRecordModelParentProperty("Carrier type", ndefRecordModelRecord));
			}

			if(handoverCarrierRecord.hasCarrierData()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier data", String.format(N_BYTES, handoverCarrierRecord.getCarrierData().length), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier data", "", ndefRecordModelRecord));
			}
			
			return ndefRecordModelRecord;
		} else if(record instanceof HandoverRequestRecord) {
			HandoverRequestRecord handoverRequestRecord = (HandoverRequestRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			ndefRecordModelRecord.add(new NdefRecordModelProperty("Major version", Byte.toString(handoverRequestRecord.getMajorVersion()), ndefRecordModelRecord));
			ndefRecordModelRecord.add(new NdefRecordModelProperty("Minor version", Byte.toString(handoverRequestRecord.getMinorVersion()), ndefRecordModelRecord));

			ndefRecordModelRecord.add(getNode(handoverRequestRecord.getCollisionResolution(), ndefRecordModelRecord));

			NdefRecordModelParentProperty ndefRecordModelParentProperty = new NdefRecordModelParentProperty("Alternative carriers", ndefRecordModelRecord);

			List<AlternativeCarrierRecord> alternativeCarriers = handoverRequestRecord.getAlternativeCarriers();
			for(AlternativeCarrierRecord alternativeCarrierRecord : alternativeCarriers) {
				ndefRecordModelParentProperty.add(getNode(alternativeCarrierRecord, ndefRecordModelParentProperty));
			}
			
			ndefRecordModelRecord.add(ndefRecordModelParentProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			if(alternativeCarrierRecord.hasCarrierPowerState()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier power state", alternativeCarrierRecord.getCarrierPowerState().toString(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier power state", "", ndefRecordModelRecord));
			}
			if(alternativeCarrierRecord.hasCarrierDataReference()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier data reference", alternativeCarrierRecord.getCarrierDataReference(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Carrier data reference", "", ndefRecordModelRecord));
			}

			NdefRecordModelPropertyList list = new NdefRecordModelPropertyList("Auxiliary data references", "Auxiliary data reference #%d", ndefRecordModelRecord);

			List<String> auxiliaryDataReferences = alternativeCarrierRecord.getAuxiliaryDataReferences();
			for(int i = 0; i < auxiliaryDataReferences.size(); i++) {
				list.add(new NdefRecordModelPropertyListItem(auxiliaryDataReferences.get(i), list));
			}
			
			ndefRecordModelRecord.add(list);
			
			return ndefRecordModelRecord;
			
			
		} else if(record instanceof CollisionResolutionRecord) {
			CollisionResolutionRecord collisionResolutionRecord = (CollisionResolutionRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			ndefRecordModelRecord.add(new NdefRecordModelProperty("Random number", Integer.toString(collisionResolutionRecord.getRandomNumber()), ndefRecordModelRecord));
			
			return ndefRecordModelRecord;
			
		} else if(record instanceof ErrorRecord) {
			ErrorRecord errorRecord = (ErrorRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			if(errorRecord.hasErrorReason()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Error Reason", errorRecord.getErrorReason().toString(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Error Reason", "", ndefRecordModelRecord));
			}
			if(errorRecord.hasErrorData()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Error Data", Long.toHexString(errorRecord.getErrorData().longValue()), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("Error Data", "", ndefRecordModelRecord));
			}
			return ndefRecordModelRecord;
		} else if(record instanceof HandoverSelectRecord) {
			HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			ndefRecordModelRecord.add(new NdefRecordModelProperty("Major version", Byte.toString(handoverSelectRecord.getMajorVersion()), ndefRecordModelRecord));
			ndefRecordModelRecord.add(new NdefRecordModelProperty("Minor version", Byte.toString(handoverSelectRecord.getMinorVersion()), ndefRecordModelRecord));

			NdefRecordModelParentProperty alternativeCarrierParentProperty = new NdefRecordModelParentProperty("Alternative carriers", ndefRecordModelRecord);

			List<AlternativeCarrierRecord> alternativeCarriers = handoverSelectRecord.getAlternativeCarriers();
			for(AlternativeCarrierRecord alternativeCarrierRecord : alternativeCarriers) {
				alternativeCarrierParentProperty.add(getNode(alternativeCarrierRecord, alternativeCarrierParentProperty));
			}
			
			ndefRecordModelRecord.add(alternativeCarrierParentProperty);

			NdefRecordModelParentProperty errorParentProperty = new NdefRecordModelParentProperty("Error", ndefRecordModelRecord);

			if(handoverSelectRecord.hasError()) {
				errorParentProperty.add(getNode(handoverSelectRecord.getError(), errorParentProperty));
			}
			
			ndefRecordModelRecord.add(errorParentProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof EmptyRecord) {
			EmptyRecord emptyRecord = (EmptyRecord)record;
				
			return new NdefRecordModelRecord(emptyRecord, ndefRecordModelParent);
		} else if(record instanceof UriRecord) {
			UriRecord uriRecord = (UriRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(uriRecord, ndefRecordModelParent);

			if(uriRecord.hasUri()) {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("URI", uriRecord.getUri(), ndefRecordModelRecord));
			} else {
				ndefRecordModelRecord.add(new NdefRecordModelProperty("URI", "", ndefRecordModelRecord));
			}
			return ndefRecordModelRecord;		
		} else if(record instanceof GenericControlRecord) {
			GenericControlRecord genericControlRecord = (GenericControlRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(genericControlRecord, ndefRecordModelParent);
			NdefRecordModelProperty ndefRecordModelProperty = new NdefRecordModelProperty("Configuration", Byte.toString(genericControlRecord.getConfigurationByte()), ndefRecordModelRecord);
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			/**
			 * The Generic Control RTD does not assume
			 * any particular order for these sub-records inside the Generic Control payload. However, it is
			 * RECOMMENDED that a Target record is specified first, an Action record is specified next, and
			 * a Data record is specified at the end for ease of readability and efficiency of processing.
			 * 
			 */
			
			ndefRecordModelRecord.add(getNode(genericControlRecord.getTarget(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(getNode(genericControlRecord.getAction(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(getNode(genericControlRecord.getData(), ndefRecordModelRecord));

			return ndefRecordModelRecord;
		} else if(record instanceof GcTargetRecord) {
			GcTargetRecord gcTargetRecord = (GcTargetRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(gcTargetRecord, ndefRecordModelParent);
			
			NdefRecordModelParentProperty ndefRecordModelParentProperty = new NdefRecordModelParentProperty("Target identifier", ndefRecordModelRecord);

			// text or uri type
			if(gcTargetRecord.hasTargetIdentifier()) {
				ndefRecordModelParentProperty.add(getNode(gcTargetRecord.getTargetIdentifier(), ndefRecordModelRecord));
			}
			
			ndefRecordModelRecord.add(ndefRecordModelParentProperty);

			return ndefRecordModelRecord;
		} else if(record instanceof GcActionRecord) {
			GcActionRecord gcActionRecord = (GcActionRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(gcActionRecord, ndefRecordModelParent);
			
			NdefRecordModelProperty ndefRecordModelProperty;
			if(gcActionRecord.hasAction()) {
				ndefRecordModelProperty = new NdefRecordModelProperty("Action", gcActionRecord.getAction().toString(), ndefRecordModelRecord);
			} else {
				ndefRecordModelProperty = new NdefRecordModelProperty("Action", "-", ndefRecordModelRecord);
			}
			
			ndefRecordModelRecord.add(ndefRecordModelProperty);

			NdefRecordModelParentProperty ndefRecordModelParentProperty = new NdefRecordModelParentProperty("ActionRecord", ndefRecordModelRecord);
			
			if(gcActionRecord.hasActionRecord()) {
				ndefRecordModelParentProperty.add(getNode(gcActionRecord.getActionRecord(), ndefRecordModelParentProperty));
			}

			ndefRecordModelRecord.add(ndefRecordModelParentProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof GcDataRecord) {

			GcDataRecord gcDataRecord = (GcDataRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(gcDataRecord, ndefRecordModelParent);

			for(Record dataRecord : gcDataRecord.getRecords()) {
				ndefRecordModelRecord.add(getNode(dataRecord, ndefRecordModelRecord));
			}

			return ndefRecordModelRecord;
		} else if(record instanceof UnsupportedRecord) {
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);
			
			//ndefRecordModelRecord.add(new NdefRecordModelProperty("TNF", Integer.toString(record.getTnf()), ndefRecordModelRecord));
			
			return ndefRecordModelRecord;
		} else if(record instanceof SignatureRecord) {
			SignatureRecord signatureRecord = (SignatureRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(signatureRecord, ndefRecordModelParent);
			
			ndefRecordModelRecord.add(new NdefRecordModelProperty("Version", Byte.toString(signatureRecord.getVersion()), ndefRecordModelRecord));

			// signature
			NdefRecordModelProperty ndefRecordModelSignatureTypeProperty;
			if(signatureRecord.hasSignatureType()) {
				ndefRecordModelSignatureTypeProperty = new NdefRecordModelProperty("Signature type", signatureRecord.getSignatureType().toString(), ndefRecordModelRecord);
			} else {
				ndefRecordModelSignatureTypeProperty = new NdefRecordModelProperty("Signature type", "-", ndefRecordModelRecord);
			}
			ndefRecordModelRecord.add(ndefRecordModelSignatureTypeProperty);

			if(!signatureRecord.isStartMarker()) {
				addNonStartMarkerNodes(signatureRecord, ndefRecordModelRecord);
			}
			
			return ndefRecordModelRecord;
		} else {
			return new NdefRecordModelRecord(record, ndefRecordModelParent);
		}
	}
	
	private static void addNonStartMarkerNodes(SignatureRecord signatureRecord, NdefRecordModelRecord ndefRecordModelRecord) {
		ndefRecordModelRecord.addChildren(getNonStartMarkerNodes(signatureRecord, ndefRecordModelRecord));
	}
	
	public static List<NdefRecordModelNode> getNonStartMarkerNodes(SignatureRecord signatureRecord, NdefRecordModelRecord ndefRecordModelRecord) {

		List<NdefRecordModelNode> nodes = new ArrayList<NdefRecordModelNode>();
		
		NdefRecordModelParentProperty ndefRecordModelParentSignatureProperty = new NdefRecordModelParentProperty("Signature", ndefRecordModelRecord);
		
		if(signatureRecord.hasSignatureUri()) {
			ndefRecordModelParentSignatureProperty.add(getNode("URI", signatureRecord.getSignatureUri(), ndefRecordModelParentSignatureProperty));
		} else if(signatureRecord.hasSignature()) {
			byte[] signature = signatureRecord.getSignature();
			if(signature != null) {
				ndefRecordModelParentSignatureProperty.add(getNode("Embedded value", String.format(N_BYTES, signature.length), ndefRecordModelParentSignatureProperty));
			} else {
				ndefRecordModelParentSignatureProperty.add(getNode("Embedded value", "-", ndefRecordModelParentSignatureProperty));
			}
		}
		nodes.add(ndefRecordModelParentSignatureProperty);

		// certificates
		NdefRecordModelProperty ndefRecordModelCertificateFormatProperty;
		if(signatureRecord.hasCertificateFormat()) {
			ndefRecordModelCertificateFormatProperty = new NdefRecordModelProperty("Certificate format", signatureRecord.getCertificateFormat().toString(), ndefRecordModelRecord);
		} else {
			ndefRecordModelCertificateFormatProperty = new NdefRecordModelProperty("Certificate format", "-", ndefRecordModelRecord);
		}
		nodes.add(ndefRecordModelCertificateFormatProperty);
					
		// add list
		NdefRecordModelPropertyList list = new NdefRecordModelPropertyList("Certificate chain", "Certificate #%d", ndefRecordModelRecord);

		List<byte[]> certificates = signatureRecord.getCertificates();
		for(int i = 0; i < certificates.size(); i++) {
			list.add(new NdefRecordModelPropertyListItem(String.format(N_BYTES, certificates.get(i).length), list));
		}
		
		nodes.add(list);
		
		if(signatureRecord.hasCertificateUri()) {
			nodes.add(getNode("Certificate chain URI", signatureRecord.getCertificateUri(), ndefRecordModelRecord));
		} else {
			nodes.add(getNode("Certificate chain URI", "", ndefRecordModelRecord));
		}
		return nodes;
	}
}
