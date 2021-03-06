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

package org.nfc.eclipse.plugin.terminal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.nfc.eclipse.plugin.Activator;
import org.nfc.eclipse.plugin.NdefEditorPart;
import org.nfc.eclipse.plugin.NdefMultiPageEditor;
import org.nfctools.NfcAdapter;
import org.nfctools.api.Tag;
import org.nfctools.api.UnknownTagListener;
import org.nfctools.mf.classic.MfClassicNfcTagListener;
import org.nfctools.mf.ul.Type2NfcTagListener;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefOperations;
import org.nfctools.ndef.NdefOperationsListener;
import org.nfctools.ndef.Record;
import org.nfctools.scio.Terminal;
import org.nfctools.scio.TerminalHandler;
import org.nfctools.scio.TerminalMode;
import org.nfctools.scio.TerminalStatus;
import org.nfctools.scio.TerminalStatusListener;
import org.nfctools.spi.acs.AcsTerminal;
import org.nfctools.spi.scm.SclTerminal;


public class NdefTerminalDetector implements Runnable, NdefOperationsListener, TerminalStatusListener, UnknownTagListener {

	private static NdefTerminalDetector detector;

	public static void initialize() {
		detector = new NdefTerminalDetector();
	}
	
	public static NdefTerminalDetector getInstance() {
		return detector;
	}
	
	private Terminal currentTerminal;
	
	private TerminalHandler terminalHandler;
	
	private NfcAdapter nfcAdapter;
	
	private boolean close = false;
		
	private NdefTerminalListener ndefTerminalReadListener;

	private NdefTerminalListener ndefTerminalWriteListener;

	private NdefOperations ndefOperations;
	
	private TerminalStatus terminalStatus = null;
	
	private int counter = 0;
	
	private Thread thread;
	
	public NdefTerminalDetector() {
		terminalHandler = new TerminalHandler();
		terminalHandler.addTerminal(new AcsTerminal());
		terminalHandler.addTerminal(new SclTerminal());
	}
	
	public boolean detectTerminal() {
		
		synchronized(this) {
			Terminal terminal = terminalHandler.getAvailableTerminal();
			
			if(currentTerminal == terminal) {
				return false;
			}
			
			if(currentTerminal != null) {
				stopReader();
			}
			
			if(terminal != null) {
				NdefTerminalWrapper.setSeenTerminal(true);
				
				startReader(terminal);
			}
			return true;
		}

	}

	private void startReader(Terminal terminal) {
		synchronized(this) {
			log("Starting terminal " + terminal.getTerminalName());
			
			currentTerminal = terminal;
			currentTerminal.setStatusListener(this);
			nfcAdapter = new NfcAdapter(terminal, TerminalMode.INITIATOR);
			
			nfcAdapter.registerTagListener(new MfClassicNfcTagListener(this));
			nfcAdapter.registerTagListener(new Type2NfcTagListener(this));
			nfcAdapter.registerUnknownTagListerner(this);
			nfcAdapter.startListening();
		}
	}

	public void notfiyChange() {
		log("Notify change in card terminal status");
		
		// notify status line if editor is open
    	Display.getDefault().asyncExec(
                new Runnable()
                {
                    public void run()
                    {
                    	IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    	if(activePage != null) {
                    		IEditorPart activeEditor = activePage.getActiveEditor();
		
	                    	if(activeEditor != null) {
	                    		if(activeEditor instanceof NdefMultiPageEditor) {
	                    			NdefMultiPageEditor ndefMultiPageEditor = (NdefMultiPageEditor)activeEditor;
	                    			
	                    			Object selectedPage = ndefMultiPageEditor.getSelectedPage();
	                    			if(selectedPage instanceof NdefEditorPart) {
	                    				NdefEditorPart ndefEditorPart = (NdefEditorPart)selectedPage;
	                    				
	                    				ndefEditorPart.refreshStatusLine();
	                    			}
	                    		}
	                    	}

                    	}
                    }
                }
            );
	}

	private void log(String message) {
		Activator activator = Activator.getDefault();		
		
		if(activator != null) {
			Activator.info(message);
		}
			
	}

	public void stopReader() {
		synchronized(this) {
			log("Stopping terminal " + currentTerminal.getTerminalName());
			if(nfcAdapter != null) {
				nfcAdapter.stopListening();
				
				nfcAdapter = null;
			}
			currentTerminal = null;
			ndefOperations = null;
		}
	}
	
	public void startDetecting() {
		if(thread == null) {
			close = false;
			
			thread = new Thread(this);
			thread.start();
		}
		notfiyChange();
	}
	
	public void stopDetecting() {
		log("Stop detecting card terminals");
		
		close = true;
		
		Thread thread = this.thread;
		thread.interrupt();
		
		stopReader();
		
		notfiyChange();
	}

	@Override
	public void run() {
		log("Start detecting card terminals");

		try {
			while(!close) {
				try {
					if(detectTerminal()) {
						notfiyChange();
					}
				} catch(RuntimeException e) {
					// usually caused by SCARD_E_NO_READERS_AVAILABLE on list()
					// ignore
				}
				try {
					// sleep for less time (be more responsive) if we know the user has an NFC reader
					if(NdefTerminalWrapper.hasSeenReader()) {
						Thread.sleep(1000);
					} else {
						Thread.sleep(5000);
					}
				} catch (InterruptedException e) {
					
				}
			}
		} finally {
			thread = null;
		}
	}

	private void openNewEditor(final byte[] encode) {
		log("Open NDEF content in new editor");

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				
				// TODO add tag id and type
				// if some tag id i already open, activate its editor TODO
				
				IStorage storage = new NdefTerminalStorage(encode, currentTerminal.getTerminalName() + "-" + counter++); // TODO file name counter i temporary solution
				IStorageEditorInput input = new NdefTerminalInput(storage, currentTerminal.getTerminalName());

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				if (page != null) {
					try {
						page.openEditor(input, NdefMultiPageEditor.class.getName());
						
						setStatus("Read tag successful.");
					} catch (PartInitException e) {
						log(e.toString());

						// do nothing more
					}
				} else {
					log("No active page for opening editor");
				}
			}
		});
	}
	
	private void setStatus(final String message) {
		// notify status line if editor is open
    	Display.getDefault().asyncExec(
                new Runnable()
                {
                    public void run()
                    {
                    	IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    	if(activePage != null) {
                    		IEditorPart activeEditor = activePage.getActiveEditor();
		
	                    	if(activeEditor != null) {
	                    		if(activeEditor instanceof NdefMultiPageEditor) {
	                    			NdefMultiPageEditor ndefMultiPageEditor = (NdefMultiPageEditor)activeEditor;
	                    			
	                    			Object selectedPage = ndefMultiPageEditor.getSelectedPage();
	                    			if(selectedPage instanceof NdefEditorPart) {
	                    				NdefEditorPart ndefEditorPart = (NdefEditorPart)selectedPage;
	                    				
	                    				ndefEditorPart.setStatus(message);
	                    			}
	                    		}
	                    	}

                    	}
                    }
                }
            );


		
	}

	// http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_programmatically%3F
	// http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_on_something_that_is_not_a_file%3F
	// http://eclipsesnippets.blogspot.no/2008/06/programmatically-opening-editor.html
	// http://stackoverflow.com/questions/171824/programmatically-showing-a-view-from-an-eclipse-plug-in
	
	@Override
	public void onNdefOperations(NdefOperations ndefOperations) {
		log("onNdefOperations");
		
		synchronized(this) {
			this.ndefOperations = ndefOperations;
			
			if(ndefTerminalWriteListener != null && ndefTerminalReadListener != null) {
				// read first then write, if not the same editor
				read(false);
				if(ndefTerminalWriteListener != ndefTerminalReadListener) {
					write();
				}
			} else if(ndefTerminalWriteListener != null) {
				write();
			} else if(ndefTerminalReadListener != null) {
				read(false);
			} else {
				read(true);
			}
		}
	}
	
	private void read(boolean newEditor) {
		List<Record> list; 
		if (ndefOperations.isFormatted()) {
			if (ndefOperations.hasNdefMessage()) {
				list = ndefOperations.readNdefMessage();
			} else {
				log("Empty formatted tag. Size: " + ndefOperations.getMaxSize() + " bytes");
				
				 list = new ArrayList<Record>();
			}
		} else {
			log("Empty tag. NOT formatted. Size: " + ndefOperations.getMaxSize() + " bytes");
			
			 list = new ArrayList<Record>();
		}
		
		if(newEditor) {
			log("Read NDEF into new editor");
			
			final byte[] encode = NdefContext.getNdefMessageEncoder().encode(list);

			openNewEditor(encode);
		} else {
			log("Read NDEF into open editor " + ndefTerminalReadListener.getClass().getSimpleName());
			
			ndefTerminalReadListener.setNdefContent(list);
			
			setStatus("Auto-read successful.");
		}

	}
	
	private void write() {
		log("Write NDEF from editor " + ndefTerminalWriteListener.getClass().getSimpleName());

		List<Record> records = ndefTerminalWriteListener.getNdefRecords();
		
		if(ndefOperations != null) {
    		try {
    			NdefContext.getNdefMessageEncoder().encode(records);

				if(ndefOperations.isFormatted()) {
					ndefOperations.writeNdefMessage(records.toArray(new Record[records.size()]));
				} else {
					ndefOperations.format(records.toArray(new Record[records.size()]));
				}
    			setStatus("Auto-write successful.");
    		} catch(Exception e) {
    			setStatus("Auto-write not possible.");
    		}
		}
	}

	@Override
	public void onStatusChanged(TerminalStatus status) {

		synchronized(this) {
			if(this.terminalStatus != status) {
				/*
				if(status == TerminalStatus.CLOSED) {
					stopReader();
					
					notfiyChange();
				} else 
				*/
				if(status == TerminalStatus.CONNECTED) {
					setStatus("Tag connected.");
				} else if(status == TerminalStatus.DISCONNECTED) {
					setStatus("Tag disconnected.");
					
					ndefOperations = null;
				} else if(status == TerminalStatus.WAITING) {
					// do nothing
				}
				this.terminalStatus = status;
			}
		}
	}

	public NdefTerminalListener getNdefTerminalReadListener() {
		return ndefTerminalReadListener;
	}

	public void setNdefTerminalReadListener(NdefTerminalListener ndefTerminalReadListener) {
		synchronized(this) {
			this.ndefTerminalReadListener = ndefTerminalReadListener;
		}
	}
	
	public NdefTerminalListener getNdefTerminalWriteListener() {
		return ndefTerminalWriteListener;
	}

	public void setNdefTerminalWriteListener(NdefTerminalListener ndefTerminalWriteListener) {
		synchronized(this) {
			this.ndefTerminalWriteListener = ndefTerminalWriteListener;
		}
	}

	public String getTerminalName() {
		synchronized(this) {
			if(currentTerminal != null) {
				return currentTerminal.getTerminalName();
			}
			return null;
		}
	}

	public NdefOperations getNdefOperations() {
		return ndefOperations;
	}

	public TerminalStatus getTerminalStatus() {
		return terminalStatus;
	}

	@Override
	public void unsupportedTag(Tag tag) {
		setStatus("Unsupported tag of type " + tag.getTagType() + " detected");
	};
	
	
}
