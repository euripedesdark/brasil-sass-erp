import React, { forwardRef } from 'react';
import { DataTable as PrimeDataTable } from 'primereact/datatable/datatable.esm.js';

/**
 * PrimeReact DataTable assumes that value/frozenValue are arrays and calls
 * Array.prototype.slice() during pagination. The ERP has legacy endpoints
 * returning either arrays, PageResponse objects, or { data: ... } envelopes.
 *
 * Keep that compatibility at the UI boundary so one malformed/legacy response
 * cannot crash the whole screen with "slice is not a function".
 */
const asCollection = (value) => {
    if (Array.isArray(value)) return value;
    if (!value || typeof value !== 'object') return [];
    if (Array.isArray(value.content)) return value.content;
    if (Array.isArray(value.data)) return value.data;
    if (value.data && typeof value.data === 'object') return asCollection(value.data);
    return [];
};

export const DataTable = forwardRef(function SafeDataTable({ value, frozenValue, ...props }, ref) {
    return (
        <PrimeDataTable
            ref={ref}
            {...props}
            value={asCollection(value)}
            frozenValue={frozenValue == null ? frozenValue : asCollection(frozenValue)}
        />
    );
});

DataTable.displayName = 'SafeDataTable';

export default DataTable;
