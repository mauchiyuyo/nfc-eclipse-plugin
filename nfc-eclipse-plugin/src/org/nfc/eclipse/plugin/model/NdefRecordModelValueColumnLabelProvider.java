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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.nfc.eclipse.plugin.model.editing.NdefRecordModelEditingSupport;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.mime.TextMimeRecord;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.TextRecord;


public class NdefRecordModelValueColumnLabelProvider extends ColumnLabelProvider {

	public NdefRecordModelValueColumnLabelProvider() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getText(Object element) {
		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;

			Record record = ndefRecordModelProperty.getRecord();
			if(record instanceof GcActionRecord) {
				GcActionRecord gcActionRecord = (GcActionRecord)record;
				if(!gcActionRecord.hasAction()) {
					return "Select action ..";
				}
			} else if(record instanceof ActionRecord) {
				ActionRecord actionRecord = (ActionRecord)record;
				if(!actionRecord.hasAction()) {
					return "Select action ..";
				}
			} else if(record instanceof ErrorRecord) {
				int parentIndex = ndefRecordModelProperty.getParentIndex();
				if(parentIndex == 1) {
					ErrorRecord errorRecord = (ErrorRecord)record;
					if(errorRecord.hasErrorData()) {
						return "0x" + Long.toHexString(errorRecord.getErrorData().longValue());
					} else {
						return "";
					}
				}
			}

			return ndefRecordModelProperty.getValue();
		} else if(element instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)element;

			Record record = ndefRecordModelRecord.getRecord();

			byte[] id = record.getId();
			if(id == null || id.length == 0) {
				return "ID";
			}
			return record.getKey();
		} else if(element instanceof NdefRecordModelParentProperty) {
			NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)element;

			NdefRecordModelParent parent = ndefRecordModelParentProperty.getParent();
			if(parent instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)parent;

				Record record = ndefRecordModelRecord.getRecord();

				if(record instanceof GcTargetRecord)  {
					GcTargetRecord gcTargetRecord = (GcTargetRecord)record;

					if(gcTargetRecord.hasTargetIdentifier()) {
						return NdefRecordType.getType(gcTargetRecord.getTargetIdentifier().getClass()).getRecordLabel();
					} else {
						return "Select target identifier..";
					}
				} else if(record instanceof GcActionRecord) {
					GcActionRecord gcActionRecord = (GcActionRecord)record;

					if(gcActionRecord.hasActionRecord()) {
						return NdefRecordType.getType(gcActionRecord.getActionRecord().getClass()).getRecordLabel();
					} else {
						return "Select action record..";
					}
				} else if(record instanceof HandoverCarrierRecord) {
					HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;

					if(handoverCarrierRecord.hasCarrierType()) {
						if(handoverCarrierRecord.getCarrierType() instanceof Record) {

							return NdefRecordType.getType((Class<? extends Record>) handoverCarrierRecord.getCarrierType().getClass()).getRecordLabel();
						} else {
							return "-";
						}
					} else {
						if(!handoverCarrierRecord.hasCarrierTypeFormat()) {
							return "Select carrier type format..";
						} else {


							switch (handoverCarrierRecord.getCarrierTypeFormat()) {
							case WellKnown: {
								// NFC Forum well-known type [NFC RTD]

								return "Select well-known record..";
							}
							case External: {
								// NFC Forum external type [NFC RTD]
								return "Select external type record..";
							}
							default: {
								throw new IllegalArgumentException();
							}
							}
						}
					}
				} else if(record instanceof HandoverSelectRecord) {
					HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;

					if(ndefRecordModelParentProperty.getParentIndex() == 3) {
						if(handoverSelectRecord.hasError()) {
							return NdefRecordModelEditingSupport.PRESENT_OR_NOT[0];
						} else {
							return NdefRecordModelEditingSupport.PRESENT_OR_NOT[1];
						}
					}
				} else if(record instanceof SignatureRecord) {
					SignatureRecord signatureRecord = (SignatureRecord)record;
					
					int index = ndefRecordModelParentProperty.getParentIndex();
					
					if(index == 2) { // signature mode
						
						if(!signatureRecord.hasSignature() && !signatureRecord.hasSignatureUri()) {
							return "Select nature";
						}
						
						if(signatureRecord.hasSignature()) {
							return "Embedded";
						}

						if(signatureRecord.hasSignatureUri()) {
							return "Linked";
						}

					} else if(index == 4) { // certificates mode

						List<byte[]> certificates = signatureRecord.getCertificates();
						if((certificates == null || certificates.size() == 0) && !signatureRecord.hasCertificateUri()) {
							return "Select nature";
						}
						
						if((certificates != null && !certificates.isEmpty())) {
							return "Embedded";
						}

						if(signatureRecord.hasCertificateUri()) {
							return "Linked";
						}
					}
				}
			}
		} else if(element instanceof NdefRecordModelPropertyListItem) {
			NdefRecordModelPropertyListItem ndefRecordModelPropertyListItem = (NdefRecordModelPropertyListItem)element;

			return ndefRecordModelPropertyListItem.getValue();
		}

		return null;
	}

	@Override
	public Color getForeground(Object element) {

		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;

			// System.out.println("Get element " + element + " label " + ndefRecordModelProperty.getValue());

			Record record = ndefRecordModelProperty.getRecord();
			if(record instanceof GcActionRecord) {
				GcActionRecord gcActionRecord = (GcActionRecord)record;
				if(!gcActionRecord.hasAction()) {
					return new Color(Display.getCurrent(), 0xBB, 0xBB, 0xBB); 
				}
			} else if(record instanceof ActionRecord) {
				ActionRecord actionRecord = (ActionRecord)record;
				if(!actionRecord.hasAction()) {
					return new Color(Display.getCurrent(), 0xBB, 0xBB, 0xBB); 
				}
			}

		} else if(element instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)element;

			Record record = ndefRecordModelRecord.getRecord();

			byte[] id = record.getId();
			if(id == null || id.length == 0) {
				return new Color(Display.getCurrent(), 0xBB, 0xBB, 0xBB); 
			}
		} else if(element instanceof NdefRecordModelParentProperty) {
			NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)element;

			NdefRecordModelParent parent = ndefRecordModelParentProperty.getParent();
			if(parent instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)parent;

				Record record = ndefRecordModelRecord.getRecord();

				if(record instanceof GcTargetRecord)  {
					GcTargetRecord gcTargetRecord = (GcTargetRecord)record;

					if(!gcTargetRecord.hasTargetIdentifier()) {
						return new Color(Display.getCurrent(), 0xBB, 0xBB, 0xBB); 
					}
				} else if(record instanceof GcActionRecord) {
					GcActionRecord gcActionRecord = (GcActionRecord)record;

					if(!gcActionRecord.hasActionRecord()) {
						return new Color(Display.getCurrent(), 0xBB, 0xBB, 0xBB); 
					}
				} else if(record instanceof HandoverCarrierRecord) {
					HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;

					if(!handoverCarrierRecord.hasCarrierType()) {
						return new Color(Display.getCurrent(), 0xBB, 0xBB, 0xBB); 
					}
				}
			}
		}
		return super.getForeground(element);
	}

	@Override
	public String getToolTipText(Object element) {
		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;

			Record record = ndefRecordModelProperty.getRecord();
			if(record instanceof MimeRecord) {
				if(ndefRecordModelProperty.getParentIndex() == 1) {
					MimeRecord mimeRecord = (MimeRecord)record;

					if(mimeRecord.hasContentType()) {
						String contentType = mimeRecord.getContentType();
						if(contentType.startsWith("text/")) {

							if(mimeRecord instanceof TextMimeRecord) {
								TextMimeRecord textMimeRecord = (TextMimeRecord)mimeRecord;
								
								String content = textMimeRecord.getContent(); // null or not
								if(content != null) {
									return content;
								}
								
							} else if(mimeRecord instanceof BinaryMimeRecord) {
								BinaryMimeRecord binaryMimeRecord = (BinaryMimeRecord)mimeRecord;
								
								byte[] content = binaryMimeRecord.getContent();
								
								if(content != null) {
									try {
										return new String(content);
									} catch(Exception e) {
										// ignore
									}
								}
							} else {
								throw new IllegalArgumentException();
							}
						}
					}
				}
			} else if(record instanceof TextRecord) {
				int parentIndex = ndefRecordModelProperty.getParentIndex();
				if(parentIndex == 0) {
					TextRecord textRecord = (TextRecord)record;

					if(textRecord.hasText()) {
						String text = textRecord.getText();
						if(text.length() > 0) {
							return text;
						}
					}
				} else if(parentIndex == 1) {
					TextRecord textRecord = (TextRecord)record;

					if(textRecord.hasLocale()) {
						return textRecord.getLocale().getDisplayName();
					}
				}
			}

		} else if(element instanceof NdefRecordModelPropertyListItem) {
			NdefRecordModelPropertyListItem node = (NdefRecordModelPropertyListItem)element;

			Record record = node.getRecord();
			if(record instanceof SignatureRecord) {
				SignatureRecord signatureRecord = (SignatureRecord)record;
				
				byte[] certificateBytes = signatureRecord.getCertificates().get(node.getParentIndex());
				
				if(signatureRecord.getCertificateFormat() == CertificateFormat.X_509) {

					try {
						if (Security.getProvider("BC") == null) {
				            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				        }

						java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509", "BC");

						return cf.generateCertificate(new ByteArrayInputStream(certificateBytes)).toString();
					} catch (Exception e) {
						// ignore
					}
				}
				
			}			
		}
		return super.getToolTipText(element);
	}

	@Override
	public Point getToolTipShift(Object object) {
		return new Point(10, 10);
	}

	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 2000;
	}

	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return 0;
	}

	@Override
	public Image getToolTipImage(Object element) {
		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;

			Record record = ndefRecordModelProperty.getRecord();
			if(record instanceof MimeRecord) {
				if(ndefRecordModelProperty.getParentIndex() == 1) {
					MimeRecord mimeRecord = (MimeRecord)record;

					if(mimeRecord.hasContentType()) {
						String contentType = mimeRecord.getContentType();
						if(contentType.startsWith("image/")) {

							if(mimeRecord instanceof BinaryMimeRecord) {
								BinaryMimeRecord binaryMimeRecord = (BinaryMimeRecord)mimeRecord;
								
								byte[] content = binaryMimeRecord.getContent();
								
								if(content != null && content.length > 0) {
									try {
										BufferedInputStream inputStreamReader = new BufferedInputStream(new ByteArrayInputStream(content));
										ImageData imageData = new ImageData(inputStreamReader);
										return new Image(Display.getCurrent(), imageData );
									} catch(Exception e) {
										// ignore
									}
								}
							}
						}
					}
				}
			}
		}

		return super.getToolTipImage(element);
	}
}
