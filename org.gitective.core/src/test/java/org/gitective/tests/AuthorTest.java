/*
 * Copyright (c) 2011 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.gitective.tests;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.AndCommitFilter;
import org.gitective.core.filter.commit.AuthorFilter;
import org.gitective.core.filter.commit.AuthorSetFilter;
import org.gitective.core.filter.commit.CommitListFilter;
import org.junit.Test;

/**
 * Unit tests of author filters
 */
public class AuthorTest extends GitTestCase {

	/**
	 * Test clone of {@link AuthorFilter}
	 */
	@Test
	public void cloneAuthorFilter() {
		AuthorFilter filter = new AuthorFilter(author);
		RevFilter cloned = filter.clone();
		assertNotNull(cloned);
		assertNotSame(filter, cloned);
		assertTrue(cloned instanceof AuthorFilter);
	}

	/**
	 * Test clone of {@link AuthorSetFilter}
	 */
	@Test
	public void cloneAuthorSetFilter() {
		AuthorSetFilter filter = new AuthorSetFilter();
		RevFilter cloned = filter.clone();
		assertNotNull(cloned);
		assertNotSame(filter, cloned);
		assertTrue(cloned instanceof AuthorSetFilter);
	}

	/**
	 * Test of {@link AuthorFilter}
	 *
	 * @throws Exception
	 */
	@Test
	public void authorFilter() throws Exception {
		add("file.txt", "a");
		PersonIdent findUser = new PersonIdent("find user", "find@user.com");
		author = findUser;
		RevCommit commit1 = add("file.txt", "b");
		RevCommit commit2 = add("file.txt", "c");
		AuthorFilter filter = new AuthorFilter(findUser);
		CommitListFilter commits = new CommitListFilter();
		CommitFinder service = new CommitFinder(testRepo);
		service.setFilter(new AndCommitFilter(filter, commits));
		service.find();
		assertEquals(2, commits.getCommits().size());
		assertEquals(commit2, commits.getCommits().get(0));
		assertEquals(commit1, commits.getCommits().get(1));
	}

	/**
	 * Test non-match of {@link AuthorFilter}
	 *
	 * @throws Exception
	 */
	@Test
	public void nonMatch() throws Exception {
		add("file.txt", "a");
		AuthorFilter filter = new AuthorFilter("not the author",
				"not@author.org");
		CommitListFilter commits = new CommitListFilter();
		CommitFinder service = new CommitFinder(testRepo);
		service.setFilter(new AndCommitFilter(filter, commits));
		service.find();
		assertEquals(0, commits.getCommits().size());
	}

	/**
	 * Test of {@link AuthorSetFilter}
	 *
	 * @throws Exception
	 */
	@Test
	public void authorSetFilter() throws Exception {
		add("file.txt", "a");
		PersonIdent findUser = new PersonIdent("find user", "find@user.com");
		author = findUser;
		add("file.txt", "b");
		AuthorSetFilter filter = new AuthorSetFilter();
		assertTrue(filter.getPersons().isEmpty());
		assertFalse(filter.getPersons().contains(findUser));
		CommitFinder service = new CommitFinder(testRepo);
		service.setFilter(filter);
		service.find();
		assertEquals(2, filter.getPersons().size());
		assertTrue(filter.getPersons().contains(findUser));
	}

	/**
	 * Match author name only
	 *
	 * @throws Exception
	 */
	@Test
	public void matchNameOnly() throws Exception {
		add("file.txt", "a");
		author = new PersonIdent("franklin", "test@test.com");
		RevCommit commit2 = add("file.txt", "b");
		author = new PersonIdent("franklin", "test@test.org");
		RevCommit commit3 = add("file.txt", "c");
		AuthorFilter filter = new AuthorFilter("franklin", null);
		CommitListFilter commits = new CommitListFilter();
		CommitFinder service = new CommitFinder(testRepo);
		service.setFilter(new AndCommitFilter().add(filter, commits));
		service.find();
		assertEquals(2, commits.getCommits().size());
		assertTrue(commits.getCommits().contains(commit2));
		assertTrue(commits.getCommits().contains(commit3));
	}

	/**
	 * Match author e-mail only
	 *
	 * @throws Exception
	 */
	@Test
	public void matchEmailOnly() throws Exception {
		add("file.txt", "a");
		author = new PersonIdent("a user", "test@test.com");
		RevCommit commit2 = add("file.txt", "b");
		author = new PersonIdent("b user", "test@test.com");
		RevCommit commit3 = add("file.txt", "c");
		AuthorFilter filter = new AuthorFilter(null, "test@test.com");
		CommitListFilter commits = new CommitListFilter();
		CommitFinder service = new CommitFinder(testRepo);
		service.setFilter(new AndCommitFilter().add(filter, commits));
		service.find();
		assertEquals(2, commits.getCommits().size());
		assertTrue(commits.getCommits().contains(commit2));
		assertTrue(commits.getCommits().contains(commit3));
	}

	/**
	 * Test of {@link AuthorSetFilter#reset()}
	 *
	 * @throws Exception
	 */
	@Test
	public void authorSetFilterReset() throws Exception {
		add("file.txt", "a");
		AuthorSetFilter filter = new AuthorSetFilter();
		assertTrue(filter.getPersons().isEmpty());
		assertFalse(filter.getPersons().contains(author));
		CommitFinder service = new CommitFinder(testRepo);
		service.setFilter(filter);
		service.find();
		assertEquals(1, filter.getPersons().size());
		assertTrue(filter.getPersons().contains(author));
		filter.reset();
		assertTrue(filter.getPersons().isEmpty());
		assertFalse(filter.getPersons().contains(author));
	}
}
