package org.bouncycastle.jsse.provider;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.util.Set;

import org.bouncycastle.jsse.java.security.BCCryptoPrimitive;

class DisabledAlgorithmConstraints
    extends AbstractAlgorithmConstraints
{
    private static final String INCLUDE_PREFIX = "include ";

    static DisabledAlgorithmConstraints create(AlgorithmDecomposer decomposer, String propertyName, String defaultValue)
    {
        String[] algorithms = PropertyUtils.getStringArraySecurityProperty(propertyName, defaultValue);
        if (null == algorithms)
        {
            return null;
        }

        // TODO[jsse] SunJSSE now supports "include jdk.disabled.namedCurves"
        for (int i = 0; i < algorithms.length; ++i)
        {
            if (algorithms[i].regionMatches(true, 0, INCLUDE_PREFIX, 0, INCLUDE_PREFIX.length()))
            {
                algorithms[i] = null;
                continue;
            }

            // TODO[jsse] Try to parse as a logical constraint (and null out if parsed)
        }

        return new DisabledAlgorithmConstraints(decomposer, asUnmodifiableSet(algorithms));
    }

    private final Set<String> disabledAlgorithms;

    private DisabledAlgorithmConstraints(AlgorithmDecomposer decomposer, Set<String> disabledAlgorithms)
    {
        super(decomposer);

        this.disabledAlgorithms = disabledAlgorithms; 
    }

    public final boolean permits(Set<BCCryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters)
    {
        checkPrimitives(primitives);
        checkAlgorithmName(algorithm);

        if (containsAnyPartIgnoreCase(disabledAlgorithms, algorithm))
        {
            return false;
        }

        if (null == parameters)
        {
            return true;
        }

        // TODO[jsse] Check whether the constraints permit these parameters for the given algorithm
        return true;
    }

    public final boolean permits(Set<BCCryptoPrimitive> primitives, Key key)
    {
        return checkConstraints(primitives, null, key, null);
    }

    public final boolean permits(Set<BCCryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters)
    {
        checkAlgorithmName(algorithm);

        return checkConstraints(primitives, algorithm, key, parameters);
    }

    private boolean checkConstraints(Set<BCCryptoPrimitive> primitives, String algorithm, Key key,
        AlgorithmParameters parameters)
    {
        checkPrimitives(primitives);
        checkKey(key);

        if (isAlgorithmSpecified(algorithm)
            && !permits(primitives, algorithm, parameters))
        {
            return false;
        }

        if (!permits(primitives, key.getAlgorithm(), null))
        {
            return false;
        }

        // TODO[jsse] Check whether key size constraints permit the given key
        return true;
    }
}
