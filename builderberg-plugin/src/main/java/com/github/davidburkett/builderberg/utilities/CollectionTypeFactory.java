package com.github.davidburkett.builderberg.utilities;

import com.github.davidburkett.builderberg.enums.CollectionType;
import com.intellij.psi.PsiType;

import java.util.Optional;

public class CollectionTypeFactory {

    public static Optional<CollectionType> getCollectionType(final PsiType type) {
        if (TypeUtility.isMap(type)) {
            if (TypeUtility.isOfType(type, CollectionType.SORTED_MAP.getCanonicalName())) {
                return Optional.of(CollectionType.SORTED_MAP);
            } else if (TypeUtility.isOfType(type, CollectionType.NAVIGABLE_MAP.getCanonicalName())) {
                return Optional.of(CollectionType.NAVIGABLE_MAP);
            }

            return Optional.of(CollectionType.MAP);
        } else if (TypeUtility.isList(type)) {
            return Optional.of(CollectionType.LIST);
        } else if (TypeUtility.isSet(type)) {
            if (TypeUtility.isOfType(type, CollectionType.SORTED_SET.getCanonicalName())) {
                return Optional.of(CollectionType.SORTED_SET);
            } else if (TypeUtility.isOfType(type, CollectionType.NAVIGABLE_SET.getCanonicalName())) {
                return Optional.of(CollectionType.NAVIGABLE_SET);
            }

            return Optional.of(CollectionType.SET);
        } else if (TypeUtility.isCollection(type)) {
            return Optional.of(CollectionType.COLLECTION);
        }

        return Optional.empty();
    }
}
