/**
 * Copyright (c) 2011 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import spdxspreadsheet.TestPackageInfoSheet;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Source Auditor
 *
 */
public class TestSPDXFile {

	static final String[] NONSTD_IDS = new String[] {SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"1",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"2", SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"3",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"4"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] STD_IDS = new String[] {"AFL-3.0", "CECILL-B", "EUPL-1.0"};
	static final String[] STD_TEXTS = new String[] {"std text1", "std text2", "std text3"};

	SPDXNonStandardLicense[] NON_STD_LICENSES;
	SPDXStandardLicense[] STANDARD_LICENSES;
	SPDXDisjunctiveLicenseSet[] DISJUNCTIVE_LICENSES;
	SPDXConjunctiveLicenseSet[] CONJUNCTIVE_LICENSES;
	
	SPDXConjunctiveLicenseSet COMPLEX_LICENSE;
	
	Resource[] NON_STD_LICENSES_RESOURCES;
	Resource[] STANDARD_LICENSES_RESOURCES;
	Resource[] DISJUNCTIVE_LICENSES_RESOURCES;
	Resource[] CONJUNCTIVE_LICENSES_RESOURCES;
	Resource COMPLEX_LICENSE_RESOURCE;
	
	Model model;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		NON_STD_LICENSES = new SPDXNonStandardLicense[NONSTD_IDS.length];
		for (int i = 0; i < NONSTD_IDS.length; i++) {
			NON_STD_LICENSES[i] = new SPDXNonStandardLicense(NONSTD_IDS[i], NONSTD_TEXTS[i]);
		}
		
		STANDARD_LICENSES = new SPDXStandardLicense[STD_IDS.length];
		for (int i = 0; i < STD_IDS.length; i++) {
			STANDARD_LICENSES[i] = new SPDXStandardLicense("Name "+String.valueOf(i), 
					STD_IDS[i], STD_TEXTS[i], new String[] {"URL "+String.valueOf(i)}, "Notes "+String.valueOf(i), 
					"LicHeader "+String.valueOf(i), "Template "+String.valueOf(i), true);
		}
		
		DISJUNCTIVE_LICENSES = new SPDXDisjunctiveLicenseSet[3];
		CONJUNCTIVE_LICENSES = new SPDXConjunctiveLicenseSet[2];
		
		DISJUNCTIVE_LICENSES[0] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				NON_STD_LICENSES[0], NON_STD_LICENSES[1], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[0] = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
				STANDARD_LICENSES[0], NON_STD_LICENSES[0], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[1] = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
				DISJUNCTIVE_LICENSES[0], NON_STD_LICENSES[2]
		});
		DISJUNCTIVE_LICENSES[1] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				CONJUNCTIVE_LICENSES[1], NON_STD_LICENSES[0], STANDARD_LICENSES[0]
		});
		DISJUNCTIVE_LICENSES[2] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				DISJUNCTIVE_LICENSES[1], CONJUNCTIVE_LICENSES[0], STANDARD_LICENSES[2]
		});
		COMPLEX_LICENSE = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
				DISJUNCTIVE_LICENSES[2], NON_STD_LICENSES[2], CONJUNCTIVE_LICENSES[1]
		});
		model = ModelFactory.createDefaultModel();
		
		NON_STD_LICENSES_RESOURCES = new Resource[NON_STD_LICENSES.length];
		for (int i = 0; i < NON_STD_LICENSES.length; i++) {
			NON_STD_LICENSES_RESOURCES[i] = NON_STD_LICENSES[i].createResource(model);
		}
		STANDARD_LICENSES_RESOURCES = new Resource[STANDARD_LICENSES.length];
		for (int i = 0; i < STANDARD_LICENSES.length; i++) {
			STANDARD_LICENSES_RESOURCES[i] = STANDARD_LICENSES[i].createResource(model);
		}
		CONJUNCTIVE_LICENSES_RESOURCES = new Resource[CONJUNCTIVE_LICENSES.length];
		for (int i = 0; i < CONJUNCTIVE_LICENSES.length; i++) {
			CONJUNCTIVE_LICENSES_RESOURCES[i] = CONJUNCTIVE_LICENSES[i].createResource(model);
		}
		DISJUNCTIVE_LICENSES_RESOURCES = new Resource[DISJUNCTIVE_LICENSES.length];
		for (int i = 0; i < DISJUNCTIVE_LICENSES.length; i++) {
			DISJUNCTIVE_LICENSES_RESOURCES[i] = DISJUNCTIVE_LICENSES[i].createResource(model);
		}
		COMPLEX_LICENSE_RESOURCE = COMPLEX_LICENSE.createResource(model);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXFile#populateModel(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws IOException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testPopulateModel() throws IOException, InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String pkgUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082?pkg";
		doc.createSpdxPackage(pkgUri);
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		Resource pkgResource = model.getResource(pkgUri);
		Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_PACKAGE_FILE);
		
		SPDXLicenseInfo[] declaredLic = new SPDXLicenseInfo[] {COMPLEX_LICENSE};
		SPDXLicenseInfo[] seenLic = new SPDXLicenseInfo[] {STANDARD_LICENSES[0]};
		String[] contributors = new String[] {"Contrib1", "Contrib2"};
		DOAPProject[] artifactOfs = new DOAPProject[] {new DOAPProject("Artifactof Project", "ArtifactOf homepage")};
		SPDXFile fileDep1 = new SPDXFile("fileDep1", "SOURCE", "1123456789abcdef0123456789abcdef01234567", 
				COMPLEX_LICENSE, seenLic, "License comments", 
				"Copyrights", artifactOfs, "file comments");
		SPDXFile fileDep2 = new SPDXFile("fileDep2", "BINARY", "2123456789abcdef0123456789abcdef01234567", 
				COMPLEX_LICENSE, seenLic, "License comments", 
				"Copyrights", artifactOfs, "file comments");
		SPDXFile[] fileDependencies = new SPDXFile[] {fileDep1, fileDep2};
		String fileNotice = "File Notice";
		SPDXFile file = new SPDXFile("fileName", "SOURCE", "0123456789abcdef0123456789abcdef01234567", 
				COMPLEX_LICENSE, seenLic, "License comments", 
				"Copyrights", artifactOfs, "file comments", fileDependencies, contributors, fileNotice);
		ArrayList<String> verify = file.verify();
		assertEquals(0, verify.size());
		Resource fileResource = file.createResource(model);
		pkgResource.addProperty(p, fileResource);
		writer = new StringWriter();
		model.write(writer);
		String afterFileCreate = writer.toString();
		writer.close();
		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		assertEquals(file.getArtifactOf()[0].getName(), file2.getArtifactOf()[0].getName());
		assertEquals(file.getArtifactOf()[0].getHomePage(), file2.getArtifactOf()[0].getHomePage());
		assertEquals(file.getCopyright(), file2.getCopyright());
		assertEquals(file.getLicenseComments(), file2.getLicenseComments());
		assertEquals(file.getComment(), file2.getComment());
		assertEquals(file.getName(), file2.getName());
		assertEquals(file.getSha1(), file2.getSha1());
		assertEquals(file.getType(), file2.getType());
		assertEquals(file.getConcludedLicenses(), file2.getConcludedLicenses());
		assertEquals(file.getNoticeText(), file2.getNoticeText());
		assertStringArraysEqual(contributors, file2.getContributors());
		TestPackageInfoSheet.compareLicenseDeclarations(file.getSeenLicenses(), file2.getSeenLicenses());
		assertFileArraysEqual(file.getFileDependencies(), file2.getFileDependencies());
		verify = file2.verify();
		assertEquals(0, verify.size());
	}

	@Test
	public void testNoneCopyright() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXFile file = new SPDXFile("filename", "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NONE_VALUE, new DOAPProject[0]);
		Resource fileResource = file.createResource(model);
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT).asNode();
		Triple m = Triple.createMatch(null, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			assertTrue(t.getObject().isURI());
			assertEquals(SpdxRdfConstants.URI_VALUE_NONE, t.getObject().getURI());
		}
		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		assertEquals(SpdxRdfConstants.NONE_VALUE, file2.getCopyright());
	}
	
	@Test
	public void testNoassertionCopyright() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXFile file = new SPDXFile("filename", "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0]);
		Resource fileResource = file.createResource(model);
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT).asNode();
		Triple m = Triple.createMatch(null, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			assertTrue(t.getObject().isURI());
			assertEquals(SpdxRdfConstants.URI_VALUE_NOASSERTION, t.getObject().getURI());
		}
		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		assertEquals(SpdxRdfConstants.NOASSERTION_VALUE, file2.getCopyright());
	}
	
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		String COMMENT1 = "comment1";
		String COMMENT2 = "comment2";
		String COMMENT3 = "comment3";
		SPDXFile file = new SPDXFile("filename", "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0], COMMENT1);
		assertEquals(file.getComment(), COMMENT1);
		Resource fileResource = file.createResource(model);
		file.setLicenseComments("see if this works");
		file.setComment(COMMENT2);
		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		assertEquals(file2.getComment(), COMMENT2);
		file2.setComment(COMMENT3);
		assertEquals(file2.getComment(), COMMENT3);
	}
	
	@Test
	public void testSetContributors() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		String CONTRIBUTOR1 = "Contributor 1";
		String CONTRIBUTOR2 = "Contributor 2";
		String CONTRIBUTOR3 = "Contributor 3";
		String[] contributors = new String[] {CONTRIBUTOR1, CONTRIBUTOR2, CONTRIBUTOR3};
		
		String CONTRIBUTOR4 = "Contributor 4";
		String[] oneContributor = new String[] {CONTRIBUTOR4};
		SPDXFile file = new SPDXFile("filename", "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0], "",
										new SPDXFile[0], oneContributor, null);
		assertEquals(1, file.getContributors().length);
		String[] result = file.getContributors();
		Resource fileResource = file.createResource(model);
		file.setContributors(contributors);
		result = file.getContributors();
		assertStringArraysEqual(contributors, result);
		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		result = file2.getContributors();
		assertStringArraysEqual(contributors, result);
		file2.setContributors(new String[0]);
		assertEquals(0, file2.getContributors().length);
	}
	
	@Test
	public void testSetNoticeText() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		String fileNotice = "This is a file notice";
		SPDXFile file = new SPDXFile("filename", "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0], "",
										new SPDXFile[0], null, null);
		if (file.getNoticeText() != null && !file.getNoticeText().isEmpty()) {
			fail("nto null notice text");
		}
		Resource fileResource = file.createResource(model);
		file.setNoticeText(fileNotice);
		String result  = file.getNoticeText();
		assertEquals(fileNotice, result);
		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		result = file2.getNoticeText();
		assertEquals(fileNotice, file2.getNoticeText());
		file2.setNoticeText(null);
		if (file2.getNoticeText() != null && !file2.getNoticeText().isEmpty()) {
			fail("nto null notice text");
		}
	}
	
	/**
	 * @param s1
	 * @param s2
	 */
	private void assertStringArraysEqual(String[] s1, String[] s2) {
		assertEquals(s1.length, s2.length);
		for (int i = 0; i < s1.length; i++) {
			boolean found = false;
			for (int j = 0; j < s2.length; j++) {
				if (s1[i].equals(s2[j])) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}

	@Test
	public void testSetFileDependencies() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		String FileDependencyName1  = "Dependency1";
		String FileDependencyName2 = "dependencies/Dependency2";
		String FileDependencyName3 = "Depenedency3";
		String COMMENT1 = "comment";
		SPDXFile file = new SPDXFile("filename", "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0], COMMENT1);
		SPDXFile fileDependency1 = new SPDXFile(FileDependencyName1, "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0], COMMENT1);
		SPDXFile fileDependency2 = new SPDXFile(FileDependencyName2, "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0], COMMENT1);
		SPDXFile fileDependency3 = new SPDXFile(FileDependencyName3, "BINARY", "sha1", COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NOASSERTION_VALUE, new DOAPProject[0], COMMENT1);
		Resource fileResource = file.createResource(model);
		Resource fileDependencyResource1 = fileDependency1.createResource(model);
		Resource fileDependencyResource2 = fileDependency2.createResource(model);
		Resource fileDependencyResource3 = fileDependency3.createResource(model);
		SPDXFile[] fileDependencies = new SPDXFile[] {fileDependency1, fileDependency2, fileDependency3};
		SPDXFile[] noDependencies = file.getFileDependencies();
		assertEquals(0, noDependencies.length);
		file.setFileDependencies(fileDependencies);
		SPDXFile[] result = file.getFileDependencies();
		assertFileArraysEqual(fileDependencies, result);
		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		result = file2.getFileDependencies();
		assertFileArraysEqual(fileDependencies, result);
	}

	/**
	 * Compares the content of the two arrays and asserts that they are equal ignoring the 
	 * order of the elements in the array
	 * @param files1
	 * @param files2
	 */
	private void assertFileArraysEqual(SPDXFile[] files1,
			SPDXFile[] files2) {
		assertEquals(files1.length, files2.length);
		for (int i = 0; i < files1.length; i++) {
			SPDXFile compareFile1 = files1[i];
			boolean found = false;
			for (int j = 0; j < files2.length; j++) {
				if (files2[j].equals(compareFile1)) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}
	
	@Test
	public void testFindFileResource() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		String FILE1_NAME = "./file/name/name1";
		String FILE1_SHA1 = "sha1";
		String FILE2_NAME = "./file2/name/name2";
		String FILE2_SHA1 = "sha2";
		String FILE3_NAME = "./a/different/name";
		String FILE4_SHA1 = "sha4";
		SPDXFile file1 = new SPDXFile(FILE1_NAME, "BINARY", FILE1_SHA1, COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NONE_VALUE, new DOAPProject[0]);
		Resource file1Resource = file1.createResource(model);
		SPDXFile file2 = new SPDXFile(FILE2_NAME, "BINARY", FILE2_SHA1, COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NONE_VALUE, new DOAPProject[0]);
		Resource file2Resource = file2.createResource(model);
		
		SPDXFile testFile1 = new SPDXFile(FILE1_NAME, "BINARY", FILE1_SHA1, COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NONE_VALUE, new DOAPProject[0]);
		SPDXFile testFile2 = new SPDXFile(FILE2_NAME, "BINARY", FILE2_SHA1, COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NONE_VALUE, new DOAPProject[0]);
		SPDXFile testFile3 = new SPDXFile(FILE3_NAME, "BINARY", FILE1_SHA1, COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NONE_VALUE, new DOAPProject[0]);
		SPDXFile testFile4 = new SPDXFile(FILE3_NAME, "BINARY", FILE4_SHA1, COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, "", SpdxRdfConstants.NONE_VALUE, new DOAPProject[0]);
		Resource retval = SPDXFile.findFileResource(model, testFile1);
		assertEquals(file1Resource, retval);
		retval = SPDXFile.findFileResource(model, testFile2);
		assertEquals(file2Resource, retval);
		retval = SPDXFile.findFileResource(model, testFile3);
		if (retval != null) {
			fail("Should be null due to different file names");
		}
		retval = SPDXFile.findFileResource(model, testFile4);
		if (retval != null) {
			fail("Should be null due to different checksums");
		}
	}
}
