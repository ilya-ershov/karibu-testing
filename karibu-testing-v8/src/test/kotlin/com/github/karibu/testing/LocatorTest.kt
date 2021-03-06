package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.label
import com.github.vok.karibudsl.textField
import com.github.vok.karibudsl.verticalLayout
import com.vaadin.ui.*
import kotlin.test.expect

class LocatorTest : DynaTest({

    beforeEach { MockVaadin.setup() }

    group("_get") {
        test("FailsOnNoComponents") {
            expectThrows(IllegalArgumentException::class) {
                Button()._get(Label::class.java)
            }
        }

        test("fails when multiple components match") {
            expectThrows(IllegalArgumentException::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._get(VerticalLayout::class.java)
            }
        }

        test("selects self") {
            val button = Button()
            expect(button) { button._get(Button::class.java) }
        }

        test("ReturnsNested") {
            val button = Button()
            expect(button) { VerticalLayout(button)._get(Button::class.java) }
        }
    }

    group("_find") {
        test("findMatchingId") {
            val button = Button().apply { id = "foo" }
            expect(listOf(button)) { VerticalLayout(button, Button())._find<Button> { id = "foo" } }
        }
    }

    group("_expectNone") {
        test("succeeds on no matched components") {
            Button()._expectNone(Label::class.java)
        }

        test("fails when multiple components match") {
            expectThrows(IllegalArgumentException::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._expectNone(VerticalLayout::class.java)
            }
        }

        test("selects self") {
            expectThrows(IllegalArgumentException::class) { Button()._expectNone(Button::class.java) }
        }

        test("ReturnsNested") {
            expectThrows(IllegalArgumentException::class) { VerticalLayout(Button())._expectNone(Button::class.java) }
        }
    }

    test("simpleUITest") {
        lateinit var layout: VerticalLayout
        layout = UI.getCurrent().verticalLayout {
            val name = textField {
                caption = "Type your name here:"
            }
            button("Click Me") {
                addClickListener {
                    println("Thanks ${name.value}, it works!")
                    layout.label("Thanks ${name.value}, it works!")
                }
            }
        }

        _get<TextField> { caption = "Type your name here:" }.value = "Baron Vladimir Harkonnen"
        _get<Button> { caption = "Click Me" }._click()
        expect("Thanks Baron Vladimir Harkonnen, it works!") { _get<Label>().value }
        expect("Thanks Baron Vladimir Harkonnen, it works!") { (layout.last() as Label).value }
        expect(3) { layout.componentCount }
    }

    group("matcher") {
        fun Component.matches(spec: SearchSpec<Component>.()->Unit): Boolean = SearchSpec(Component::class.java).apply { spec() }.toPredicate().invoke(this)
        test("caption") {
            expect(true) { Button("click me").matches { caption = "click me" } }
            expect(true) { TextField("name:").matches { caption = "name:" } }
            expect(true) { Button("click me").matches { } }
            expect(true) { TextField("name:").matches { } }
            expect(false) { Button("click me").matches { caption = "Click Me" } }
            expect(false) { TextField("name:").matches { caption = "Name"} }
        }
        test("placeholder") {
            expect(true) { TextField("name").apply { placeholder = "the name" } .matches { placeholder = "the name" } }
            expect(true) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { placeholder = "at least 6 characters" } }
            expect(true) { ComboBox<String>().apply { placeholder = "foo" }.matches { placeholder = "foo" } }
            expect(false) { TextField("name").apply { placeholder = "the name" } .matches { placeholder = "name" } }
            expect(false) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { placeholder = "password" } }
        }
    }
})
