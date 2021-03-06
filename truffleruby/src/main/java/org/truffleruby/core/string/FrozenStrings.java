/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.Rope;

import java.util.Map;
import java.util.WeakHashMap;

public class FrozenStrings {

    private final RubyContext context;

    private final Map<RopeHolder, DynamicObject> frozenStrings = new WeakHashMap<>();

    public FrozenStrings(RubyContext context) {
        this.context = context;
    }

    public synchronized DynamicObject getFrozenString(Rope rope) {
        assert context.getRopeTable().contains(rope);

        final RopeHolder holder = new RopeHolder(rope);
        DynamicObject string = frozenStrings.get(holder);

        if (string == null) {
            string = Layouts.STRING.createString(context.getCoreLibrary().getFrozenStringFactory(), rope);
            frozenStrings.put(holder, string);
        }

        return string;
    }

    // TODO (nirvdrum 29-Nov-2016) This is a temporary measure to cope with Rope#equals not taking Encoding into consideration. Fixing that is a much more involved effort, but once completed this wrapper class can be removed.
    private static class RopeHolder {

        private final Rope rope;

        public RopeHolder(Rope rope) {
            this.rope = rope;
        }

        public Rope getRope() {
            return rope;
        }

        @Override
        public int hashCode() {
            return rope.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RopeHolder) {
                final RopeHolder other = (RopeHolder) o;

                return rope.getEncoding() == other.getRope().getEncoding() && rope.equals(other.getRope());
            }

            return false;
        }
    }
}
