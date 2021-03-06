package com.github.karibu.mockhttp

import com.github.mvysny.dynatest.DynaTest
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import kotlin.test.expect

class MockContextTest : DynaTest({
    lateinit var ctx: ServletContext
    beforeEach { ctx = MockContext() }

    test("attributes") {
        expect(null) { ctx.getAttribute("foo") }
        ctx.setAttribute("foo", "bar")
        expect("bar") { ctx.getAttribute("foo") }
        ctx.setAttribute("foo", null)
        expect(null) { ctx.getAttribute("foo") }
        ctx.setAttribute("foo", "bar")
        expect("bar") { ctx.getAttribute("foo") }
        ctx.removeAttribute("foo")
        expect(null) { ctx.getAttribute("foo") }
    }
})